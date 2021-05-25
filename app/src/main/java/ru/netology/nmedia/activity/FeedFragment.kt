package ru.netology.nmedia.activity

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.SmoothScroller
import com.google.android.material.snackbar.Snackbar
import ru.netology.nmedia.R
import ru.netology.nmedia.adapter.OnInteractionListener
import ru.netology.nmedia.adapter.PostsAdapter
import ru.netology.nmedia.databinding.FragmentFeedBinding
import ru.netology.nmedia.di.DependencyContainer
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.viewmodel.PostViewModel


class FeedFragment : Fragment() {

    private var newPostsWasPressed: Boolean = false
    private val viewModel: PostViewModel by viewModels(ownerProducer = ::requireParentFragment){
        DependencyContainer.getInstance().viewModelFactory
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentFeedBinding.inflate(inflater, container, false)

        val adapter = PostsAdapter(object : OnInteractionListener {
            override fun onEdit(post: Post) {
                viewModel.edit(post)
            }

            override fun onLike(post: Post) {
                viewModel.likeById(post.id)
            }

            override fun onRemove(post: Post) {
                viewModel.removeById(post.id)
            }

            override fun onShare(post: Post) {
                val intent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, post.content)
                    type = "text/plain"
                }

                val shareIntent =
                    Intent.createChooser(intent, getString(R.string.chooser_share_post))
                startActivity(shareIntent)
            }

            override fun onShowPicAttachment(post: Post) {
                viewModel.selectedId = post.id
                findNavController().navigate(R.id.action_feedFragment_to_picFragment)
            }
        })
        binding.recyclerView.adapter = adapter

        viewModel.dataState.observe(viewLifecycleOwner, { state ->
            binding.progress.isVisible = state.loading
            binding.swiperefresh.isRefreshing = state.refreshing
            if (state.error) {
                Snackbar.make(binding.root, R.string.error_loading, Snackbar.LENGTH_LONG)
                    .setAction(R.string.retry_loading) { viewModel.loadPosts() }
                    .show()
            }
        })

        viewModel.data.observe(viewLifecycleOwner) { state ->
            adapter.submitList(state.posts) {
                if (newPostsWasPressed) {
                    val lm = binding.recyclerView.layoutManager
                    lm?.smoothScrollToPosition(binding.recyclerView, RecyclerView.State(), 0)
                    newPostsWasPressed = false
                }
            }
            binding.emptyText.isVisible = state.empty
        }

        binding.swiperefresh.setOnRefreshListener {
            viewModel.refreshPosts()
            viewModel.updateWasSeen()
        }

        binding.fab.setOnClickListener {
            findNavController().navigate(R.id.action_feedFragment_to_newPostFragment)
        }

        binding.fabNewPosts.setOnClickListener {
            viewModel.updateWasSeen()
            binding.fabNewPosts.isVisible = false
            newPostsWasPressed = true
        }

        viewModel.newerCount.observe(viewLifecycleOwner) {
            println("------------------------------------------")
            println(it)
            binding.fabNewPosts.isVisible = it > 0
        }

        return binding.root
    }
}

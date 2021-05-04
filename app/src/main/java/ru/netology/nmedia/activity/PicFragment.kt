package ru.netology.nmedia.activity

import android.os.Bundle
import android.view.*
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import ru.netology.nmedia.R
import ru.netology.nmedia.databinding.FragmentPicBinding
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.util.AndroidUtils
import ru.netology.nmedia.util.StringArg
import ru.netology.nmedia.viewmodel.PostViewModel

class PicFragment : Fragment() {

    companion object {
        var Bundle.textArg: String? by StringArg
    }

    private val viewModel: PostViewModel by viewModels(
        ownerProducer = ::requireParentFragment
    )

    private var fragmentBinding: FragmentPicBinding? = null
    private var idOnScreen: Long? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_new_post, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.save -> {
                fragmentBinding?.let {
                    viewModel.save()
                    AndroidUtils.hideKeyboard(requireView())
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentPicBinding.inflate(
            inflater,
            container,
            false
        )
        fragmentBinding = binding

        viewModel.data.observe(viewLifecycleOwner) { state ->
            val post = viewModel.data.value!!.posts.firstOrNull { x -> x.id == viewModel.selectedId }
            if (post != null) {
                if (post.id != idOnScreen) {
                    idOnScreen = post.id
                    if (post.attachment != null) {
                        val urlAttachment = "http://10.0.2.2:9999/media/${post.attachment.url}"
                        Glide.with(binding.picViewAttachment)
                            .load(urlAttachment)
                            .placeholder(R.drawable.common_full_open_on_phone)
                            .error(R.drawable.ic_baseline_error_24)
                            .timeout(10_000)
                            .into(binding.picViewAttachment)
                    }
                }
                binding.like.text = post.likes.toString()
                binding.like.isChecked = post.likedByMe
            }
        }

        binding.like.setOnClickListener {
            binding.like.isChecked = !binding.like.isChecked
            val post =
                viewModel.data.value!!.posts.firstOrNull { x -> x.id == viewModel.selectedId }
            if (post != null) {
                viewModel.likeById(post.id)
            }
        }

        return binding.root
    }

    override fun onDestroyView() {
        fragmentBinding = null
        super.onDestroyView()
    }
}
package ru.netology.nmedia.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.paging.LoadState
import androidx.paging.LoadStateAdapter
import androidx.recyclerview.widget.RecyclerView
import ru.netology.nmedia.databinding.LoadStateBinding

class ProgressAdapter(private val listener: ProgressAdapter.OnInteractionListener) : LoadStateAdapter<ProgressViewHolder>(){
    override fun onBindViewHolder(holder: ProgressViewHolder, loadState: LoadState) {
        holder.bind(loadState)
    }

    override fun onCreateViewHolder(parent: ViewGroup, loadState: LoadState): ProgressViewHolder {
        return ProgressViewHolder(
            LoadStateBinding.inflate(
                LayoutInflater.from(parent.context), parent, false),
            listener)
    }


    interface OnInteractionListener {
        fun onRetry() {}
    }
}


class ProgressViewHolder(
    private val binding: LoadStateBinding,
    private val listener: ProgressAdapter.OnInteractionListener,
): RecyclerView.ViewHolder(binding.root){
    fun bind(loadState: LoadState){
        with(binding){
            progress.isVisible = loadState == LoadState.Loading
            retry.isVisible = loadState is LoadState.Error
            retry.setOnClickListener {
                listener.onRetry()
            }
        }
    }
}


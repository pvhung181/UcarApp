package com.pvhung.ucar.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.pvhung.ucar.data.model.HistoryItem
import com.pvhung.ucar.databinding.ItemHistoryBinding
import com.pvhung.ucar.utils.DateUtils
import com.pvhung.ucar.utils.beInvisible
import com.pvhung.ucar.utils.beVisible

class HistoryAdapter(
    val context: Context,
    val isDriver: Boolean = true,
    val listener: HistoryClickListener? = null

) : RecyclerView.Adapter<HistoryAdapter.ViewHolder>() {

    private val mDiffer: AsyncListDiffer<HistoryItem>

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding: ItemHistoryBinding =
            ItemHistoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindData(mDiffer.currentList[position], position)
    }

    override fun getItemCount(): Int {
        return mDiffer.currentList.size
    }

    fun submit(list: List<HistoryItem>) {
        mDiffer.submitList(list)
    }

    inner class ViewHolder(private val binding: ItemHistoryBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bindData(historyItem: HistoryItem, position: Int) {
            if (isDriver) {
                binding.tvViewReview.beVisible()
                binding.tvReview.beInvisible()
            } else {
                if (historyItem.rating == 0.0 && historyItem.review.isEmpty()) {
                    binding.tvViewReview.beInvisible()
                    binding.tvReview.beVisible()
                } else {
                    binding.tvViewReview.beVisible()
                    binding.tvReview.beInvisible()
                }
            }

            binding.tvDestination.text = historyItem.destination
            binding.tvDistance.text = "${String.format("%.2f", historyItem.distance)}km"
            binding.tvMoney.text = "${String.format("$%.2f", historyItem.money)}"

            if (historyItem.time != 0L)
                binding.tvTime.text = DateUtils.convertMillisToFormattedDate(historyItem.time)



            binding.tvReview.setOnClickListener {
                listener?.onReviewClick(historyItem)
            }

            binding.tvViewReview.setOnClickListener {
                listener?.onViewReviewClick(
                    historyItem.rating.toInt(),
                    historyItem.review
                )
            }
        }
    }

    private val diffCallback: DiffUtil.ItemCallback<HistoryItem?> =
        object : DiffUtil.ItemCallback<HistoryItem?>() {
            override fun areItemsTheSame(oldItem: HistoryItem, newItem: HistoryItem): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: HistoryItem, newItem: HistoryItem): Boolean {
                return oldItem == newItem
            }
        }

    init {
        mDiffer = AsyncListDiffer<HistoryItem>(this, diffCallback)
    }

    interface HistoryClickListener {
        fun onReviewClick(historyItem: HistoryItem)
        fun onViewReviewClick(rating: Int, review: String)
    }

}
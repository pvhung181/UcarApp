package com.pvhung.ucar.ui.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.pvhung.ucar.R
import com.pvhung.ucar.common.enums.SettingType
import com.pvhung.ucar.databinding.ItemSettingBinding
import com.pvhung.ucar.interfaces.OnSettingListener

class SettingsAdapter(
    val context: Context,
    private val list: List<SettingType>,
    val listener: OnSettingListener
) : RecyclerView.Adapter<SettingsAdapter.ViewHolder>() {


//    private var list: List<SettingType> = emptyList()
//
//    @SuppressLint("NotifyDataSetChanged")
//    fun setData(lst: List<SettingType>) {
//        list = lst
//        notifyDataSetChanged()
//    }

    inner class ViewHolder(itemView: ItemSettingBinding) : RecyclerView.ViewHolder(itemView.root) {
        val binding = itemView
        fun bindData(type: SettingType) {
            when (type) {
                SettingType.SIGN_OUT -> {
                    binding.tvSetting.text = ContextCompat.getString(context, R.string.sign_out)
                    binding.ivIcon.setImageDrawable(
                        ContextCompat.getDrawable(
                            context,
                            R.drawable.ic_sign_out
                        )
                    )
                }
            }

            binding.settingBtn.setOnClickListener {
                listener.onItemClick(type)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemSettingBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindData(list[position])
    }
}
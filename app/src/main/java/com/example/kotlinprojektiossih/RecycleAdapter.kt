package com.example.kotlinprojektiossih


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.recycle_item.view.*

class RecycleAdapter(
        private val recycleList: List<RecycleItem>,
        private val listener: OnItemClickListener
) :
    RecyclerView.Adapter<RecycleAdapter.ExampleViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExampleViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.recycle_item,
            parent, false)

        return ExampleViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ExampleViewHolder, position: Int) {
        val currentItem = recycleList[position]


        holder.textView1.text = currentItem.text1
        holder.textView2.text = currentItem.text2
        holder.textView3.text = currentItem.text3

    }

    override fun getItemCount() = recycleList.size

  inner class ExampleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) ,
          View.OnClickListener {
        val textView1: TextView = itemView.text_view_1
        val textView2: TextView = itemView.text_view_2
        val textView3: TextView = itemView.text_view_3
      init {
          itemView.setOnClickListener(this)
      }
      override fun onClick(v: View?) {
          val position = adapterPosition
          if (position != RecyclerView.NO_POSITION) {
              listener.onItemClick(position)
          }
      }
  }
    interface OnItemClickListener {
        fun onItemClick(position: Int)
    }
}
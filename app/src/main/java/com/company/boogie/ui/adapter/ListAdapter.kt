package com.company.boogie.ui.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.company.boogie.models.Product
import com.company.boogie.R
import com.company.boogie.StatusCode
import com.company.boogie.models.FirestoreProductModel

// ListActivity의 RecyclerView를 위한 어댑터
// 여러 종류 기자재의 리스트 (기자재명, 이미지)

class ListAdapter(private var products: List<Product>)
    : RecyclerView.Adapter<ListAdapter.ListViewHolder>() {

    class ListViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val listName: TextView = view.findViewById(R.id.list_item_name)
        val listImage: ImageView = view.findViewById(R.id.list_item_image)
    }

    fun interface OnItemClickListener {
        fun onItemClick(item_classification_id: Int)
    }

    private var itemClickListener: OnItemClickListener? = null

    fun setOnItemClickListener(listener: OnItemClickListener) {
        itemClickListener = listener
    }

    fun updateList(newList: List<Product>) {
        Log.d("ListAdapter", "RecylcerView 업데이트")
        products = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.list_item, parent, false)
        return ListViewHolder(view)
    }

    override fun onBindViewHolder(holder: ListViewHolder, position: Int) {
        val firestoreProductModel = FirestoreProductModel()
        val product = products[position]

        holder.listName.text = product.name

        Log.d("ListAdapter", "이미지 비트맵 ${product.img}")
        firestoreProductModel.getProductImgBitmap(product.img) { STATUS_CODE, bitmap ->
            if (STATUS_CODE == StatusCode.SUCCESS) {
                Log.d("ListAdapter", "이미지 비트맵 가져오기 성공")
                holder.listImage.setImageBitmap(bitmap)
            }
            else {
                Log.w("ListAdapter", "이미지 비트맵 가져오기 실패")
                holder.listImage.setImageResource(R.drawable.cancel)
            }
        }

        holder.listName.setOnClickListener {
            itemClickListener?.onItemClick(product.classificationCode)
        }
        holder.listImage.setOnClickListener {
            itemClickListener?.onItemClick(product.classificationCode)
        }
    }

    override fun getItemCount(): Int = products.size
}
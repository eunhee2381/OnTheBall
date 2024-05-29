package com.company.boogie.ui.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.company.boogie.R
import com.company.boogie.StatusCode
import com.company.boogie.models.FirestoreProductModel
import com.company.boogie.models.Product

// DetailListActivity의 RecyclerView를 위한 어댑터
// 한 종류 기자재의 리스트 (기자재명, 이미지, 대여현황)

class DetailListAdapter(private var products: List<Product>)
    : RecyclerView.Adapter<DetailListAdapter.DetailListViewHolder>() {

    class DetailListViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val detailListName: TextView = view.findViewById(R.id.detail_list_item_name)
        val detailListImage: ImageView = view.findViewById(R.id.detail_list_item_image)
        val detailListBorrow: TextView = view.findViewById(R.id.detail_list_item_borrow)
    }

    fun interface OnItemClickListener {
        fun onItemClick(item_document_id: String, item_can_borrow: Boolean)
    }

    private var itemClickListener: OnItemClickListener? = null

    fun setOnItemClickListener(listener: OnItemClickListener) {
        itemClickListener = listener
    }

    fun updateList(newList: List<Product>) {
        Log.d("DetailListAdapter", "RecylcerView 업데이트")
        products = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DetailListViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.detail_list_item, parent, false)
        return DetailListViewHolder(view)
    }

    override fun onBindViewHolder(holder: DetailListViewHolder, position: Int) {
        val firestoreProductModel = FirestoreProductModel()
        val product = products[position]

        holder.detailListName.text = "${product.name} ${product.productId}"
        holder.detailListBorrow.text = if (product.canBorrow) "대여 가능" else "대여 중"

        Log.d("DetailListAdapter", "이미지 비트맵 ${product.img}")
        firestoreProductModel.getProductImgBitmap(product.img) { STATUS_CODE, bitmap ->
            if (STATUS_CODE == StatusCode.SUCCESS) {
                Log.d("ListAdapter", "이미지 비트맵 가져오기 성공")
                holder.detailListImage.setImageBitmap(bitmap)
            }
            else {
                Log.w("ListAdapter", "이미지 비트맵 가져오기 실패")
                holder.detailListImage.setImageResource(R.drawable.cancel)
            }
        }

        holder.detailListName.setOnClickListener {
            itemClickListener?.onItemClick(product.documentId, product.canBorrow)
        }

        holder.detailListImage.setOnClickListener {
            itemClickListener?.onItemClick(product.documentId, product.canBorrow)
        }

        holder.detailListBorrow.setOnClickListener {
            itemClickListener?.onItemClick(product.documentId, product.canBorrow)
        }
    }

    override fun getItemCount(): Int = products.size

}
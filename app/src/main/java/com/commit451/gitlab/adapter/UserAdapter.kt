package com.commit451.gitlab.adapter

import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import com.commit451.gitlab.R
import com.commit451.gitlab.model.api.UserBasic
import com.commit451.gitlab.viewHolder.LoadingFooterViewHolder
import com.commit451.gitlab.viewHolder.UserViewHolder
import java.util.*

/**
 * Adapter for a list of users
 */
class UserAdapter(private val listener: UserAdapter.Listener) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {

        private val FOOTER_COUNT = 1

        private val TYPE_ITEM = 0
        private val TYPE_FOOTER = 1
    }

    private val values: ArrayList<UserBasic> = ArrayList()
    private var loading: Boolean = false

    val spanSizeLookup: GridLayoutManager.SpanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
        override fun getSpanSize(position: Int): Int {
            val viewType = getItemViewType(position)
            if (viewType == TYPE_FOOTER) {
                return 2
            } else {
                return 1
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        when (viewType) {
            TYPE_ITEM -> {
                val holder = UserViewHolder.inflate(parent)
                holder.itemView.setOnClickListener { v ->
                    val position = v.getTag(R.id.list_position) as Int
                    val holder = v.getTag(R.id.list_view_holder) as UserViewHolder
                    listener.onUserClicked(getUser(position), holder)
                }
                return holder
            }
            TYPE_FOOTER -> return LoadingFooterViewHolder.inflate(parent)
        }
        throw IllegalStateException("No known viewholder for type " + viewType)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is UserViewHolder) {
            holder.bind(values[position])
            holder.itemView.setTag(R.id.list_position, position)
            holder.itemView.setTag(R.id.list_view_holder, holder)
        } else if (holder is LoadingFooterViewHolder) {
            holder.bind(loading)
        }
    }

    override fun getItemViewType(position: Int): Int {
        if (position == values.size) {
            return TYPE_FOOTER
        } else {
            return TYPE_ITEM
        }
    }

    override fun getItemCount(): Int {
        return values.size + FOOTER_COUNT
    }

    fun setData(users: Collection<UserBasic>?) {
        values.clear()
        addData(users)
    }

    fun addData(users: Collection<UserBasic>?) {
        if (users != null) {
            values.addAll(users)
        }
        notifyDataSetChanged()
    }

    fun clearData() {
        values.clear()
        notifyDataSetChanged()
    }

    fun setLoading(loading: Boolean) {
        this.loading = loading
        notifyItemChanged(values.size)
    }

    private fun getUser(position: Int): UserBasic {
        return values[position]
    }

    interface Listener {
        fun onUserClicked(user: UserBasic, userViewHolder: UserViewHolder)
    }
}

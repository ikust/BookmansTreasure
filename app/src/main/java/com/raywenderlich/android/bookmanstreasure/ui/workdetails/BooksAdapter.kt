/*
 * Copyright (c) 2018 Razeware LLC
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * Notwithstanding the foregoing, you may not use, copy, modify, merge, publish,
 * distribute, sublicense, create a derivative work, and/or sell copies of the
 * Software in any work that is designed, intended, or marketed for pedagogical or
 * instructional purposes related to programming, coding, application development,
 * or information technology.  Permission for such use, copying, modification,
 * merger, publication, distribution, sublicensing, creation of derivative works,
 * or sale is expressly withheld.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.raywenderlich.android.bookmanstreasure.ui.workdetails

import android.arch.paging.PagedListAdapter
import android.support.v7.util.DiffUtil
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.RequestManager
import com.raywenderlich.android.bookmanstreasure.R
import com.raywenderlich.android.bookmanstreasure.data.Book

class BooksAdapter(
    private val glide: RequestManager,
    var itemClickListener: ((Book) -> Unit)? = null
) : PagedListAdapter<Book, BooksAdapter.ViewHolder>(BOOK_COMPARATOR) {

  class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    val cover: ImageView = view.findViewById(R.id.ivCover)
    val title: TextView = view.findViewById(R.id.tvTitle)
    val published: TextView = view.findViewById(R.id.tvPublishedYear)
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
    return ViewHolder(
        LayoutInflater.from(parent.context)
            .inflate(R.layout.item_book, parent, false)
    )
  }

  override fun onBindViewHolder(holder: ViewHolder, position: Int) {
    val item = getItem(position)

    glide.load(item?.cover?.medium)
        .error(glide.load(R.drawable.book_cover_missing))
        .into(holder.cover)

    holder.title.text = getItem(position)?.title
    holder.published.text =
        holder.itemView.context.getString(R.string.published_year, getItem(position)?.publishDate)

    holder.itemView.setOnClickListener {
      if (item != null) itemClickListener?.invoke(item)
    }
  }

  companion object {
    val BOOK_COMPARATOR = object : DiffUtil.ItemCallback<Book>() {
      override fun areContentsTheSame(oldItem: Book, newItem: Book): Boolean =
          oldItem == newItem

      override fun areItemsTheSame(oldItem: Book, newItem: Book): Boolean =
          oldItem.url == newItem.url
    }
  }
}

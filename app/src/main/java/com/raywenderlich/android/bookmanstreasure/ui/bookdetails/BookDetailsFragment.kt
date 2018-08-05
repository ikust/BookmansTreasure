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

package com.raywenderlich.android.bookmanstreasure.ui.bookdetails

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.raywenderlich.android.bookmanstreasure.R
import com.raywenderlich.android.bookmanstreasure.ui.workdetails.AuthorsAdapter
import com.raywenderlich.android.bookmanstreasure.util.initToolbar
import kotlinx.android.synthetic.main.fragment_book_details.*

class BookDetailsFragment : Fragment() {

  private lateinit var viewModel: BookDetailsViewModel

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                            savedInstanceState: Bundle?): View? {
    return inflater.inflate(R.layout.fragment_book_details, container, false)
  }

  override fun onActivityCreated(savedInstanceState: Bundle?) {
    super.onActivityCreated(savedInstanceState)
    viewModel = ViewModelProviders.of(this).get(BookDetailsViewModel::class.java)

    initToolbar(toolbar, 0, true)
    initDetails()

    viewModel.loadArguments(arguments)
  }

  private fun initDetails() {
    viewModel.book.observe(this, Observer {

      if (it?.cover?.medium != null) {
        Glide.with(this)
            .load(it.cover.medium)
            .error(Glide.with(this).load(R.drawable.book_cover_missing))
            .into(ivCover)
      } else {
        Glide.with(this)
            .load(R.drawable.book_cover_missing)
            .into(ivCover)
      }

      toolbar.title = it?.title
      toolbar.subtitle = it?.subtitle

      tvNumberOfPages.text = getString(R.string.number_of_pages, it?.numberOfPages)
      tvPublishedYear.text = getString(R.string.published_year, it?.publishDate)

      val adapter = AuthorsAdapter(it?.authors ?: ArrayList())
      adapter.itemCLickListener = {
        // TODO implement navigation to author details
      }

      rvAuthors.adapter = adapter
    })
  }
}

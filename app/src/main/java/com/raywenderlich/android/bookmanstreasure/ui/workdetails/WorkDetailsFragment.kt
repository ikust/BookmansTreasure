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

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.*
import com.bumptech.glide.Glide
import com.raywenderlich.android.bookmanstreasure.R
import com.raywenderlich.android.bookmanstreasure.data.Author
import com.raywenderlich.android.bookmanstreasure.data.Work
import com.raywenderlich.android.bookmanstreasure.source.NetworkState
import com.raywenderlich.android.bookmanstreasure.util.CoverSize
import com.raywenderlich.android.bookmanstreasure.util.initToolbar
import com.raywenderlich.android.bookmanstreasure.util.loadCover
import kotlinx.android.synthetic.main.fragment_work_details.*

class WorkDetailsFragment : Fragment() {

  private lateinit var viewModel: WorkDetailsViewModel

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setHasOptionsMenu(true)
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                            savedInstanceState: Bundle?): View? {
    return inflater.inflate(R.layout.fragment_work_details, container, false)
  }

  override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
    inflater?.inflate(R.menu.work_details, menu)

    // Make menu items invisible until details are loaded.
    menu?.findItem(R.id.menuAddFavorite)?.isVisible = false
    menu?.findItem(R.id.menuRemoveFavorite)?.isVisible = false
  }

  override fun onOptionsItemSelected(item: MenuItem?): Boolean {
    return when (item?.itemId) {
      R.id.menuAddFavorite -> viewModel.addAsFavorite()
      R.id.menuRemoveFavorite -> viewModel.removeFromFavorites()
      else -> super.onOptionsItemSelected(item)
    }
  }

  override fun onActivityCreated(savedInstanceState: Bundle?) {
    super.onActivityCreated(savedInstanceState)
    viewModel = ViewModelProviders.of(this).get(WorkDetailsViewModel::class.java)

    initToolbar(toolbar, 0, true)
    initDetails()
    initEditionsAdapter()

    toolbar.postDelayed({ viewModel.loadArguments(arguments) }, 100)
  }

  private fun initDetails() {
    viewModel.work.observe(this, Observer { work ->

      if (work?.coverId != null) {
        Glide.with(this)
            .loadCover(work.coverId, CoverSize.M)
            .error(Glide.with(this).load(R.drawable.book_cover_missing))
            .into(ivCover)
      } else {
        Glide.with(this)
            .load(R.drawable.book_cover_missing)
            .into(ivCover)
      }

      toolbar.title = work?.title
      toolbar.subtitle = work?.subtitle

      viewModel.favorite.observe(this@WorkDetailsFragment, Observer { favorite ->
        if (favorite != null) {
          toolbar.menu.findItem(R.id.menuAddFavorite)?.isVisible = false
          toolbar.menu.findItem(R.id.menuRemoveFavorite)?.isVisible = true
        } else {
          toolbar.menu.findItem(R.id.menuAddFavorite)?.isVisible = true
          toolbar.menu.findItem(R.id.menuRemoveFavorite)?.isVisible = false
        }
      })

      val adapter = AuthorsAdapter(getAuthors(work))
      adapter.itemCLickListener = {
        //TODO implement navigation to Author details
      }

      rvAuthors.adapter = adapter

      val numberOfEditions = work?.editionIsbns?.size ?: 0

      tvEditions.text = resources.getQuantityString(R.plurals.editions_available,
          numberOfEditions, numberOfEditions)
    })
  }

  private fun initEditionsAdapter() {
    val adapter = BooksAdapter(Glide.with(this))

    rvEditions.adapter = adapter
    adapter.itemClickListener = {
      //TODO Implement navigation to edition details
    }

    viewModel.data.observe(this, Observer {
      adapter.submitList(it)
    })

    viewModel.networkState.observe(this, Observer {
      progressBar.visibility = when (it) {
        NetworkState.LOADING -> View.VISIBLE
        else -> View.GONE
      }
    })
  }

  private fun getAuthors(it: Work?): List<Author> {
    val authors = ArrayList<Author>()

    if (it?.authorName?.size != null) {
      for (i in 0 until it.authorName.size) {
        authors.add(
            Author(
                it.authorName[i],
                it.authorKey[i]
            )
        )
      }
    }

    return authors
  }
}

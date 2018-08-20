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

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Transformations
import android.arch.paging.LivePagedListBuilder
import android.arch.paging.PagedList
import android.os.Bundle
import com.raywenderlich.android.bookmanstreasure.api.OpenLibraryApi
import com.raywenderlich.android.bookmanstreasure.data.Work
import com.raywenderlich.android.bookmanstreasure.repository.FavoritesRepository
import com.raywenderlich.android.bookmanstreasure.source.BookDataSourceFactory
import com.raywenderlich.android.bookmanstreasure.source.NetworkState

class WorkDetailsViewModel(app: Application) : AndroidViewModel(app) {

  companion object {
    private const val WORK_ARGUMENT = "work"

    fun createArguments(work: Work): Bundle {
      val bundle = Bundle()
      bundle.putSerializable(WORK_ARGUMENT, work)

      return bundle
    }
  }

  private val favoritesRepository = FavoritesRepository(app)

  private val bookDataSourceFactory = BookDataSourceFactory(
      OpenLibraryApi.create()
  )

  private val pagingConfig = PagedList.Config.Builder()
      .setPageSize(10)
      .setPrefetchDistance(10)
      .setEnablePlaceholders(true)
      .build()

  val data = LivePagedListBuilder(bookDataSourceFactory, pagingConfig)
      .build()

  val networkState: LiveData<NetworkState> =
      Transformations.switchMap(bookDataSourceFactory.sourceLiveData) {
        it.networkState
      }

  val work: LiveData<Work> = Transformations.map(bookDataSourceFactory.work) { it }

  var favorite: LiveData<Work> = MutableLiveData()

  fun loadArguments(arguments: Bundle?) {
    if (arguments == null) {
      return
    }

    val work: Work? = arguments.get(WORK_ARGUMENT) as Work?

    if (work != null) {
      favorite = favoritesRepository.getFavorite(work.id)

      bookDataSourceFactory.work.postValue(work)
      bookDataSourceFactory.sourceLiveData.value?.invalidate()
    }
  }

  fun addAsFavorite(): Boolean {
    val work = this.work.value

    return if (work != null) {
      favoritesRepository.addFavorite(work)
      true
    } else {
      false
    }
  }

  fun removeFromFavorites(): Boolean {
    val work = this.work.value

    return if (work != null) {
      favoritesRepository.removeFavorite(work)
      true
    } else {
      false
    }
  }
}

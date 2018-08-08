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

package com.raywenderlich.android.bookmanstreasure.ui.booksearch

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.Transformations
import android.arch.lifecycle.ViewModel
import android.arch.paging.LivePagedListBuilder
import android.arch.paging.PagedList
import com.raywenderlich.android.bookmanstreasure.api.OpenLibraryApi
import com.raywenderlich.android.bookmanstreasure.data.SearchCriteria
import com.raywenderlich.android.bookmanstreasure.source.NetworkState
import com.raywenderlich.android.bookmanstreasure.source.WorkDataSourceFactory

class BookSearchViewModel : ViewModel() {
  companion object {
    private const val PAGE_SIZE = 100
  }

  private val workDataSourceFactory = WorkDataSourceFactory(
      OpenLibraryApi.create()
  )

  private val pagingConfig = PagedList.Config.Builder()
      .setPageSize(PAGE_SIZE)
      .setPrefetchDistance(PAGE_SIZE)
      .setEnablePlaceholders(true)
      .build()

  val data = LivePagedListBuilder(workDataSourceFactory, pagingConfig)
      .build()

  val networkState: LiveData<NetworkState> = Transformations.switchMap(workDataSourceFactory.sourceLiveData) {
    it.networkState
  }

  fun updateSearchTerm(searchTerm: String) {
    workDataSourceFactory.searchTerm.postValue(searchTerm)
    workDataSourceFactory.sourceLiveData.value?.invalidate()
  }

  fun updateSearchCriteria(searchCriteria: SearchCriteria) {
    workDataSourceFactory.searchCriteria.postValue(searchCriteria)
    workDataSourceFactory.sourceLiveData.value?.invalidate()
  }
}

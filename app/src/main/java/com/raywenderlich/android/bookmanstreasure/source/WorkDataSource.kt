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

package com.raywenderlich.android.bookmanstreasure.source

import android.arch.lifecycle.MutableLiveData
import android.arch.paging.PageKeyedDataSource
import com.raywenderlich.android.bookmanstreasure.api.OpenLibraryApi
import com.raywenderlich.android.bookmanstreasure.data.SearchCriteria
import com.raywenderlich.android.bookmanstreasure.data.SearchResponse
import com.raywenderlich.android.bookmanstreasure.data.Work
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException

class WorkDataSource(
    private val openLibraryApi: OpenLibraryApi,
    private val searchTerm: String,
    private val searchCriteria: SearchCriteria
) : PageKeyedDataSource<Int, Work>() {

  val networkState = MutableLiveData<NetworkState>()

  fun getNextPageKey(firstItem: Int, itemCount: Int, pageSize: Int): Int? {
    return if (firstItem + pageSize < itemCount) {
      (firstItem + pageSize / pageSize) + 1
    } else {
      null
    }
  }

  private fun createRequest(searchTerm: String, searchCriteria: SearchCriteria, pageKey: Int) = when (searchCriteria) {
    SearchCriteria.AUTHOR -> openLibraryApi.searchByAuthor(searchTerm, pageKey)
    SearchCriteria.TITLE -> openLibraryApi.searchByTitle(searchTerm, pageKey)
    else -> openLibraryApi.search(searchTerm, pageKey)
  }

  override fun loadBefore(params: LoadParams<Int>, callback: LoadCallback<Int, Work>) {
    // Ignored, since we only ever append to our initial load
  }

  override fun loadInitial(params: LoadInitialParams<Int>, callback: LoadInitialCallback<Int, Work>) {
    networkState.postValue(NetworkState.LOADING)

    if (searchTerm.isEmpty()) {
      networkState.postValue(NetworkState.LOADED)

      callback.onResult(ArrayList<Work>(), null, null)
      return
    }

    // triggered by a refresh, we better execute sync
    try {
      val response = createRequest(
          searchTerm,
          searchCriteria,
          1
      ).execute()

      val data = response.body()
      val items = response.body()?.results ?: ArrayList()

      networkState.postValue(NetworkState.LOADED)

      callback.onResult(
          items,
          null,
          getNextPageKey(data?.start ?: 0, data?.count ?: 0, params.requestedLoadSize)
      )
    } catch (ioException: IOException) {
      ioException.printStackTrace()

      val error = NetworkState.error(ioException.message ?: "unknown error")
      networkState.postValue(error)
    }
  }

  override fun loadAfter(params: LoadParams<Int>, callback: LoadCallback<Int, Work>) {
    createRequest(
        searchTerm,
        searchCriteria,
        params.key
    ).enqueue(object : Callback<SearchResponse> {

      override fun onResponse(call: Call<SearchResponse>?, response: Response<SearchResponse>?) {
        if (response?.isSuccessful == true) {
          val data = response.body()
          val items = response.body()?.results ?: ArrayList()

          callback.onResult(
              items,
              getNextPageKey(data?.start ?: 0, data?.count ?: 0, params.requestedLoadSize)
          )
        } else {
          networkState.postValue(
              NetworkState.error("error code: ${response?.code()}"))
        }
      }

      override fun onFailure(call: Call<SearchResponse>?, t: Throwable?) {
        networkState.postValue(NetworkState.error(t?.message ?: "unknown err"))
      }
    })
  }
}

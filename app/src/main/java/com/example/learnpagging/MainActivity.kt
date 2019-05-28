package com.example.learnpagging

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.View
import com.example.learnpagging.adapter.AdapterTheMovieDb
import com.example.learnpagging.api.ApiTheMovieDb
import com.example.learnpagging.model.TheMovieDb
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import kotlin.properties.Delegates

class MainActivity : AppCompatActivity() {

    private val TAG = javaClass.simpleName
    private var adapterTheMovieDb by Delegates.notNull<AdapterTheMovieDb>()
    private var isLoading by Delegates.notNull<Boolean>()
    private var page by Delegates.notNull<Int>()
    private var totalPage by Delegates.notNull<Int>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        page = 1
        totalPage = 0
        doLoadData()
        initListener()
    }

    private fun doLoadData() {
        Log.d(TAG, "page: $page")
        showLoading(true)
        val retrofit = Retrofit.Builder()
                .baseUrl(BuildConfig.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build()

        val apiTheMovieDb = retrofit.create(ApiTheMovieDb::class.java)
        apiTheMovieDb.getNowPlaying(page = page)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        { theMovieDb: TheMovieDb ->
                            val resultTheMovieDb = theMovieDb.results as ArrayList
                            if (page == 1) {
                                adapterTheMovieDb = AdapterTheMovieDb(this@MainActivity, resultTheMovieDb)
                                recyclerView.layoutManager = LinearLayoutManager(this@MainActivity)
                                recyclerView.adapter = adapterTheMovieDb
                            } else {
                                adapterTheMovieDb.refreshAdapter(resultTheMovieDb)
                            }
                            totalPage = theMovieDb.totalPages
                        },
                        { t: Throwable ->
                            t.printStackTrace()
                        },
                        {
                            hideLoading()
                        }
                )
    }

    private fun initListener() {
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                val linearLayoutManager = recyclerView.layoutManager as LinearLayoutManager
                val countItem = linearLayoutManager.itemCount
                val lastVisiblePosition = linearLayoutManager.findLastCompletelyVisibleItemPosition()
                val isLastPosition = countItem.minus(1) == lastVisiblePosition
                Log.d(TAG, "countItem: $countItem")
                Log.d(TAG, "lastVisiblePosition: $lastVisiblePosition")
                Log.d(TAG, "isLastPosition: $isLastPosition")
                if (!isLoading && isLastPosition && page < totalPage) {
                    showLoading(true)
                    page = page.let { it.plus(1) }
                    doLoadData()
                }
            }
        })
    }

    private fun showLoading(isRefresh: Boolean) {
        isLoading = true
        progressBar.visibility = View.VISIBLE
        recyclerView.visibility.let {
            if (isRefresh) {
                View.VISIBLE
            } else {
                View.GONE
            }
        }
    }

    private fun hideLoading() {
        isLoading = false
        progressBar.visibility = View.GONE
        recyclerView.visibility = View.VISIBLE
    }
}

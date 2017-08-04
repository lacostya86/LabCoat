package com.commit451.gitlab.fragment

import android.net.Uri
import android.os.Bundle
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import butterknife.BindView
import com.commit451.gitlab.App
import com.commit451.gitlab.R
import com.commit451.gitlab.adapter.DividerItemDecoration
import com.commit451.gitlab.adapter.FeedAdapter
import com.commit451.gitlab.extension.setup
import com.commit451.gitlab.model.rss.Entry
import com.commit451.gitlab.model.rss.Feed
import com.commit451.gitlab.navigation.Navigator
import com.commit451.gitlab.rx.CustomSingleObserver
import com.novoda.simplechromecustomtabs.SimpleChromeCustomTabs
import com.trello.rxlifecycle2.android.FragmentEvent
import timber.log.Timber

/**
 * Takes an RSS feed url and shows the feed
 */
class FeedFragment : ButterKnifeFragment() {

    companion object {

        private val EXTRA_FEED_URL = "extra_feed_url"

        fun newInstance(feedUrl: String): FeedFragment {
            val args = Bundle()
            args.putString(EXTRA_FEED_URL, feedUrl)

            val fragment = FeedFragment()
            fragment.arguments = args
            return fragment
        }
    }

    @BindView(R.id.swipe_layout) lateinit var swipeRefreshLayout: SwipeRefreshLayout
    @BindView(R.id.list) lateinit var listEntries: RecyclerView
    @BindView(R.id.message_text) lateinit var textMessage: TextView

    lateinit var adapterFeed: FeedAdapter

    var feedUrl: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        feedUrl = Uri.parse(arguments.getString(EXTRA_FEED_URL))
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater!!.inflate(R.layout.fragment_feed, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapterFeed = FeedAdapter(object : FeedAdapter.Listener {
            override fun onFeedEntryClicked(entry: Entry) {
                Navigator.navigateToUrl(activity, entry.link.href, App.get().getAccount())
            }
        })
        listEntries.layoutManager = LinearLayoutManager(activity)
        listEntries.addItemDecoration(DividerItemDecoration(activity))
        listEntries.adapter = adapterFeed

        swipeRefreshLayout.setOnRefreshListener { loadData() }

        loadData()
    }

    override fun onResume() {
        super.onResume()
        SimpleChromeCustomTabs.getInstance().connectTo(activity)
    }

    override fun onPause() {
        if (SimpleChromeCustomTabs.getInstance().isConnected) {
            SimpleChromeCustomTabs.getInstance().disconnectFrom(activity)
        }
        super.onPause()
    }

    override fun loadData() {
        if (view == null) {
            return
        }
        if (feedUrl == null) {
            swipeRefreshLayout.isRefreshing = false
            textMessage.visibility = View.VISIBLE
            return
        }
        textMessage.visibility = View.GONE
        swipeRefreshLayout.isRefreshing = true
        App.get().gitLab.getFeed(feedUrl!!.toString())
                .setup(bindUntilEvent(FragmentEvent.DESTROY_VIEW))
                .subscribe(object : CustomSingleObserver<Feed>() {
                    override fun success(feed: Feed) {
                        swipeRefreshLayout.isRefreshing = false
                        if (feed.entries != null && !feed.entries.isEmpty()) {
                            textMessage.visibility = View.GONE
                        } else {
                            Timber.d("No activity in the feed")
                            textMessage.visibility = View.VISIBLE
                            textMessage.setText(R.string.no_activity)
                        }
                        adapterFeed.setEntries(feed.entries)
                    }

                    override fun error(e: Throwable) {
                        Timber.e(e)
                        swipeRefreshLayout.isRefreshing = false
                        textMessage.visibility = View.VISIBLE
                        textMessage.setText(R.string.connection_error_feed)
                        adapterFeed.setEntries(null)
                    }
                })
    }
}

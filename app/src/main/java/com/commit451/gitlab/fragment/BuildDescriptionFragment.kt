package com.commit451.gitlab.fragment

import android.os.Bundle
import android.os.Parcelable
import android.support.design.widget.Snackbar
import android.support.v4.widget.SwipeRefreshLayout
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import butterknife.BindView
import com.commit451.gitlab.App
import com.commit451.gitlab.R
import com.commit451.gitlab.event.BuildChangedEvent
import com.commit451.gitlab.extension.setup
import com.commit451.gitlab.model.api.Build
import com.commit451.gitlab.model.api.Project
import com.commit451.gitlab.model.api.RepositoryCommit
import com.commit451.gitlab.model.api.Runner
import com.commit451.gitlab.rx.CustomSingleObserver
import com.commit451.gitlab.util.DateUtil
import com.trello.rxlifecycle2.android.FragmentEvent
import org.greenrobot.eventbus.Subscribe
import org.parceler.Parcels
import timber.log.Timber
import java.util.*

/**
 * Shows the details of a build
 */
class BuildDescriptionFragment : ButterKnifeFragment() {

    companion object {

        private val KEY_PROJECT = "project"
        private val KEY_BUILD = "build"

        fun newInstance(project: Project, build: Build): BuildDescriptionFragment {
            val fragment = BuildDescriptionFragment()
            val args = Bundle()
            args.putParcelable(KEY_PROJECT, Parcels.wrap(project))
            args.putParcelable(KEY_BUILD, Parcels.wrap(build))
            fragment.arguments = args
            return fragment
        }
    }

    @BindView(R.id.root) lateinit var root: ViewGroup
    @BindView(R.id.swipe_layout) lateinit var swipeRefreshLayout: SwipeRefreshLayout
    @BindView(R.id.text_status) lateinit var textStatus: TextView
    @BindView(R.id.text_duration) lateinit var textDuration: TextView
    @BindView(R.id.text_created) lateinit var textCreated: TextView
    @BindView(R.id.text_finished) lateinit var textFinished: TextView
    @BindView(R.id.text_runner) lateinit var textRunner: TextView
    @BindView(R.id.text_ref) lateinit var textRef: TextView
    @BindView(R.id.text_author) lateinit var textAuthor: TextView
    @BindView(R.id.text_message) lateinit var textMessage: TextView

    lateinit var project: Project
    lateinit var build: Build

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        project = Parcels.unwrap<Project>(arguments.getParcelable<Parcelable>(KEY_PROJECT))
        build = Parcels.unwrap<Build>(arguments.getParcelable<Parcelable>(KEY_BUILD))
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater!!.inflate(R.layout.fragment_build_description, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        swipeRefreshLayout.setOnRefreshListener { load() }
        bindBuild(build)
        App.bus().register(this)
    }

    override fun onDestroyView() {
        App.bus().unregister(this)
        super.onDestroyView()
    }

    fun load() {
        App.get().gitLab.getBuild(project.id, build.id)
                .setup(bindUntilEvent(FragmentEvent.DESTROY_VIEW))
                .subscribe(object : CustomSingleObserver<Build>() {

                    override fun error(t: Throwable) {
                        Timber.e(t)
                        Snackbar.make(root, R.string.unable_to_load_build, Snackbar.LENGTH_LONG)
                                .show()
                    }

                    override fun success(build: Build) {
                        swipeRefreshLayout.isRefreshing = false
                        this@BuildDescriptionFragment.build = build
                        bindBuild(build)
                        App.bus().post(BuildChangedEvent(build))
                    }
                })
    }

    fun bindBuild(build: Build) {
        var finishedTime: Date? = build.finishedAt
        if (finishedTime == null) {
            finishedTime = Date()
        }
        var startedTime: Date? = build.startedAt
        if (startedTime == null) {
            startedTime = Date()
        }
        val status = String.format(getString(R.string.build_status), build.status)
        textStatus.text = status
        val timeTaken = DateUtil.getTimeTaken(startedTime, finishedTime)
        val duration = String.format(getString(R.string.build_duration), timeTaken)
        textDuration.text = duration
        val created = String.format(getString(R.string.build_created), DateUtil.getRelativeTimeSpanString(activity, build.createdAt))
        textCreated.text = created
        val ref = String.format(getString(R.string.build_ref), build.ref)
        textRef.text = ref
        if (build.finishedAt != null) {
            val finished = String.format(getString(R.string.build_finished), DateUtil.getRelativeTimeSpanString(activity, build.finishedAt))
            textFinished.text = finished
            textFinished.visibility = View.VISIBLE
        } else {
            textFinished.visibility = View.GONE
        }
        if (build.runner != null) {
            bindRunner(build.runner)
        }
        if (build.commit != null) {
            bindCommit(build.commit)
        }
    }

    fun bindRunner(runner: Runner) {
        val runnerNum = String.format(getString(R.string.runner_number), runner.id.toString())
        textRunner.text = runnerNum
    }

    fun bindCommit(commit: RepositoryCommit) {
        val authorText = String.format(getString(R.string.build_commit_author), commit.authorName)
        textAuthor.text = authorText
        val messageText = String.format(getString(R.string.build_commit_message), commit.message)
        textMessage.text = messageText
    }

    @Subscribe
    fun onBuildChangedEvent(event: BuildChangedEvent) {
        if (build.id == event.build.id) {
            build = event.build
            bindBuild(build)
        }
    }
}

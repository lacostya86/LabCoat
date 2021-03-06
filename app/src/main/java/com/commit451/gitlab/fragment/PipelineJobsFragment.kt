package com.commit451.gitlab.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.commit451.gitlab.App
import com.commit451.gitlab.R
import com.commit451.gitlab.event.PipelineChangedEvent
import com.commit451.gitlab.extension.with
import com.commit451.gitlab.model.api.CommitUser
import com.commit451.gitlab.model.api.Pipeline
import com.commit451.gitlab.model.api.Project
import com.commit451.gitlab.util.DateUtil
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.fragment_pipeline_description.*
import org.greenrobot.eventbus.Subscribe
import timber.log.Timber
import java.util.*

/**
 * Shows the details of a pipeline
 */
class PipelineJobsFragment : BaseFragment() {

    companion object {

        private const val KEY_PROJECT = "project"
        private const val KEY_PIPELINE = "pipeline"

        fun newInstance(project: Project, pipeline: Pipeline): PipelineJobsFragment {
            val fragment = PipelineJobsFragment()
            val args = Bundle()
            args.putParcelable(KEY_PROJECT, project)
            args.putParcelable(KEY_PIPELINE, pipeline)
            fragment.arguments = args
            return fragment
        }
    }

    private lateinit var project: Project
    private lateinit var pipeline: Pipeline

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        project = arguments?.getParcelable(KEY_PROJECT)!!
        pipeline = arguments?.getParcelable(KEY_PIPELINE)!!
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_pipeline_description, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        swipeRefreshLayout.setOnRefreshListener { load() }
        bindPipeline(pipeline)
        App.bus().register(this)
    }

    override fun onDestroyView() {
        App.bus().unregister(this)
        super.onDestroyView()
    }

    fun load() {
        App.get().gitLab.getPipeline(project.id, pipeline.id)
                .with(this)
                .subscribe({
                    swipeRefreshLayout.isRefreshing = false
                    pipeline = it
                    bindPipeline(pipeline)
                    App.bus().post(PipelineChangedEvent(pipeline))
                }, {
                    Timber.e(it)
                    Snackbar.make(root, R.string.unable_to_load_pipeline, Snackbar.LENGTH_LONG)
                            .show()
                })
    }

    private fun bindPipeline(pipeline: Pipeline) {
        var finishedTime: Date? = pipeline.finishedAt
        if (finishedTime == null) {
            finishedTime = Date()
        }
        var startedTime: Date? = pipeline.startedAt
        if (startedTime == null) {
            startedTime = Date()
        }
        val status = String.format(getString(R.string.pipeline_status), pipeline.status)
        textStatus.text = status

        val name = String.format(getString(R.string.pipeline_name), pipeline.id)
        textNumber.text = name

        val created = String.format(getString(R.string.build_created), DateUtil.getRelativeTimeSpanString(baseActivty, pipeline.createdAt))
        textCreated.text = created

        val timeTaken = DateUtil.getTimeTaken(startedTime, finishedTime)
        val duration = String.format(getString(R.string.pipeline_duration), timeTaken)
        textDuration.text = duration

        val ref = String.format(getString(R.string.pipeline_ref), pipeline.ref)
        textRef.text = ref

        val sha = String.format(getString(R.string.pipeline_sha), pipeline.sha)
        textSha.text = sha


        if (pipeline.finishedAt != null) {
            val finished = String.format(getString(R.string.pipeline_finished), DateUtil.getRelativeTimeSpanString(baseActivty, pipeline.finishedAt))
            textFinished.text = finished
            textFinished.visibility = View.VISIBLE
        } else {
            textFinished.visibility = View.GONE
        }
        val user = pipeline.user
        if (user != null) {
            bindUser(user)
        }
    }

    private fun bindUser(user: CommitUser) {
        val authorText = String.format(getString(R.string.pipeline_commit_author), user.name)
        textAuthor.text = authorText
    }

    @Suppress("unused")
    @Subscribe
    fun onPipelineChangedEvent(event: PipelineChangedEvent) {
        if (pipeline.id == event.pipeline.id) {
            pipeline = event.pipeline
            bindPipeline(pipeline)
        }
    }
}

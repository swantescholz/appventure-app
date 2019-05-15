package de.sscholz.appventure.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.viewpager.widget.ViewPager
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import de.sscholz.appventure.R
import de.sscholz.appventure.activities.ActivityMain
import de.sscholz.appventure.data.MyRoomDb
import de.sscholz.appventure.miscui.ViewModelTourSolving
import de.sscholz.appventure.util.*
import kotlinx.android.synthetic.main.fragment_tour_solving_main.*
import kotlin.concurrent.thread


class FragmentTourSolvingMain : Fragment() {


    private lateinit var viewModel: ViewModelTourSolving


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_tour_solving_main, container, false)
        viewModel = ViewModelProviders.of(activity!!).get(ViewModelTourSolving::class.java)
        return rootView
    }

    private fun updateViewForCurrentStation(stationIndex: Int) {
        Glide.with(context!!)
                .load(completeAssetUri(viewModel.currentStation.imageUri))
                .apply(RequestOptions()
//                        .placeholder(R.drawable.ic_image_placeholder_24dp)
                        .error(R.drawable.ic_error_24dp))
                .into(station_image)
        station_solution_edittext.setText("", TextView.BufferType.EDITABLE)
        station_title.text = getString(R.string.solving_station_title_name) + " ${stationIndex + 1}/${viewModel.currentTour.numberOfStations}: ${viewModel.currentStation.title}"
        station_description.text = viewModel.currentStation.description
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fun updatePreviousNextButtonsEnabledState() {
            button_previous_station.isEnabled = viewModel.currentStationIndex.value > 0
            if (viewModel.currentTourProgress.value != null) {
                button_next_station.isEnabled = viewModel.currentStationIndex.value < viewModel.currentTourProgress.value!!
            }
        }
        viewModel.currentStationIndex.nonNullObserve(this) { newStationIndex ->
            updateViewForCurrentStation(newStationIndex)
            updatePreviousNextButtonsEnabledState()
        }
        viewModel.currentTourProgress.nonNullObserve(this) {
            updatePreviousNextButtonsEnabledState()
        }
        station_solution_edittext.setOnEditorActionListener { _, actionId: Int, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                checkSolution()
                return@setOnEditorActionListener true
            }
            return@setOnEditorActionListener false
        }
        button_solve_station.setOnClickListener { _ ->
            checkSolution()
        }
        button_unveil_next_station.setOnClickListener {
            viewModel.unveilNextStation.value = true
            val viewPager = activity?.findViewById(R.id.view_pager) as ViewPager
            viewPager.setCurrentItem(1, true)
        }
        button_previous_station.setOnClickListener {
            if (viewModel.currentStationIndex.value == 0) {
                snackbar(getString(R.string.not_possible))
            } else {
                viewModel.setNewStationIndex(viewModel.currentStationIndex.value - 1)
            }
        }
        button_next_station.setOnClickListener {
            if (viewModel.currentStationIndex.value == viewModel.currentTourProgress.value!!) {
                snackbar(getString(R.string.next_station_not_yet_unlocked))
            } else {
                viewModel.setNewStationIndex(viewModel.currentStationIndex.value + 1)
            }
        }
    }

    private fun checkSolution() {
        val input = station_solution_edittext.text.toString()
        val foundSolution = findAcceptableSolution(viewModel.currentStation.puzzleSolutions, input)
        if (foundSolution != null) {
            view!!.hideKeyboard()
            viewModel.unveilNextStation.value = false
            if (viewModel.currentStationIndex.value < viewModel.currentTour.numberOfStations - 1) {
                val message = viewModel.currentStation.messageAfterSolving
                        ?: getString(R.string.solving_correct_message).format(foundSolution)
                AlertDialog.Builder(context!!).setTitle(getString(R.string.solving_correct_title)).setMessage(message)
                        .setPositiveButton(getString(R.string.next_station), null)
                        .setOnDismissListener {
                            viewModel.setNewStationIndex(viewModel.currentStationIndex.value + 1)
                        }.show()
            } else {
                val message = viewModel.currentStation.messageAfterSolving
                        ?: getString(R.string.solving_correct_message_final).format(foundSolution)
                AlertDialog.Builder(context!!).setTitle(getString(R.string.solving_correct_last_title)).setMessage(message)
                        .setPositiveButton(getString(R.string.main_menu), null)
                        .setOnDismissListener {
                            viewModel.resetTourProgress()
                            thread {
                                MyRoomDb.instance.pastPositionDao().deleteAllWithTourId(viewModel.currentTour.title)
                            }
                            startActivity(Intent(context!!, ActivityMain::class.java))
                        }.show()
            }

        } else {
            AlertDialog.Builder(context!!).setTitle(getString(R.string.solving_incorrect_title)).setMessage(getString(R.string.solving_incorrect_message).format(input))
                    .setPositiveButton(getString(R.string.ok), null).show()
        }
    }

}



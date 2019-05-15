package de.sscholz.appventure.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import de.sscholz.appventure.*
import de.sscholz.appventure.data.AppData
import de.sscholz.appventure.data.Tour
import de.sscholz.appventure.miscui.AdapterTourOverviewList
import de.sscholz.appventure.util.nonNullObserve


/**
 * A fragment representing a list of Items.
 * Activities containing this fragment MUST implement the
 * [FragmentTourOverviewList.OnListFragmentInteractionListener] interface.
 */
class FragmentTourOverviewList : Fragment() {

    private lateinit var recyclerViewAdapter: AdapterTourOverviewList

    private var columnCount = 1

    private var listener: OnListFragmentInteractionListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            columnCount = it.getInt(ARG_COLUMN_COUNT)
        }

        AppData.tours.nonNullObserve(this) {
            recyclerViewAdapter.mValues = it!!
            recyclerViewAdapter.notifyDataSetChanged()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_touroverview_list, container, false)

        // Set the adapter
        if (view is RecyclerView) {
            with(view) {
                val itemDecor = DividerItemDecoration(context, DividerItemDecoration.VERTICAL)
                addItemDecoration(itemDecor)
                layoutManager = when {
                    columnCount <= 1 -> LinearLayoutManager(context)
                    else -> GridLayoutManager(context, columnCount)
                }
                recyclerViewAdapter = AdapterTourOverviewList(AppData.tours.value!!, listener)
                adapter = recyclerViewAdapter
            }
        }
        return view
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnListFragmentInteractionListener) {
            listener = context
        } else {
            throw RuntimeException(context.toString() + " must implement OnListFragmentInteractionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     *
     *
     * See the Android Training lesson
     * [Communicating with Other Fragments](http://developer.android.com/training/basics/fragments/communicating.html)
     * for more information.
     */
    interface OnListFragmentInteractionListener {
        // TODO: Update argument type and name
        fun onListFragmentInteraction(tour: Tour)
    }

    companion object {

        const val ARG_COLUMN_COUNT = "column-count"

        // TODO: Customize parameter initialization
        @JvmStatic
        fun newInstance(columnCount: Int) =
                FragmentTourOverviewList().apply {
                    arguments = Bundle().apply {
                        putInt(ARG_COLUMN_COUNT, columnCount)
                    }
                }
    }
}

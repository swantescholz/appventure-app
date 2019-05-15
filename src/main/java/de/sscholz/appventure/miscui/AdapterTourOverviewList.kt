package de.sscholz.appventure.miscui


import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import de.sscholz.appventure.*
import de.sscholz.appventure.data.AppData
import de.sscholz.appventure.data.Globals
import de.sscholz.appventure.data.Tour
import de.sscholz.appventure.fragments.FragmentTourOverviewList.OnListFragmentInteractionListener
import de.sscholz.appventure.util.completeAssetUri
import de.sscholz.appventure.util.distanceTo
import de.sscholz.appventure.util.formatDistance
import de.sscholz.appventure.util.formatTime
import kotlinx.android.synthetic.main.fragment_touroverview_item.view.*


class AdapterTourOverviewList(
        var mValues: List<Tour>,
        private val mListener: OnListFragmentInteractionListener?)
    : RecyclerView.Adapter<AdapterTourOverviewList.ViewHolder>() {

    private val mOnClickListener: View.OnClickListener

    init {
        mOnClickListener = View.OnClickListener { v ->
            val item = v.tag as Tour
            // Notify the active callbacks interface (the activity, if the fragment is attached to
            // one) that an item has been selected.
            mListener?.onListFragmentInteraction(item)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.fragment_touroverview_item, parent, false)
        return ViewHolder(view)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val tour = mValues[position]
        Glide.with(holder.mView.context)
                .load(completeAssetUri(tour.imageUri)).apply(RequestOptions()
                        .placeholder(R.drawable.ic_image_placeholder_24dp)
                        .error(R.drawable.ic_error_24dp))
                .into(holder.mImage)
        holder.mImage.layoutParams.width = Globals.screenWidth * 1 / 5

        holder.mTitle.text = "${position + 1}) ${tour.title}"
        holder.mStats.text = "${formatDistance(tour.startPosition.distanceTo(AppData.currentCameraPosition.value))} entfernt\n${tour.numberOfStations} Stationen\n" +
                "${formatDistance(tour.completeDistance)} / ${formatTime(tour.estimatedCompletionTime)}"
        holder.mRankings.text = "Schwierigkeit: ${tour.difficultyRanking}/5\n" +
                "Story: ${tour.storyRanking}/5\n" +
                "Landschaft: ${tour.landscapeRanking}/5"

        with(holder.mView) {
            tag = tour
            setOnClickListener(mOnClickListener)
        }
    }

    override fun getItemCount(): Int = mValues.size

    inner class ViewHolder(val mView: View) : RecyclerView.ViewHolder(mView) {
        val mImage: ImageView = mView.item_image!!
        val mTitle: TextView = mView.item_title
        val mStats: TextView = mView.item_stats
        val mRankings: TextView = mView.item_rankings
    }
}

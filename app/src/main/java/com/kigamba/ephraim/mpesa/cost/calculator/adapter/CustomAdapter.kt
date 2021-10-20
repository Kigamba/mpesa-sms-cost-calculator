package com.kigamba.ephraim.mpesa.cost.calculator.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.kigamba.ephraim.mpesa.cost.calculator.R

/**
 * Created by Kigamba (nek.eam@gmail.com) on 20-October-2021
 */

class CustomAdapter(dataSet: HashMap<String, Float>) :
  RecyclerView.Adapter<CustomAdapter.ViewHolder>() {

  var dataSet2 : Array<Pair<String, Float>?>

  init {
    dataSet2 = arrayOfNulls<Pair<String, Float>?>(dataSet.size)

    var index = 0
    dataSet.forEach {
      dataSet2[index] = Pair(it.key, it.value)
      index++
    }
  }

  /**
   * Provide a reference to the type of views that you are using
   * (custom ViewHolder).
   */
  class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    val durationLabel: TextView
    val durationTotalTransactionsCost: TextView

    init {
      // Define click listener for the ViewHolder's View.
      durationLabel = view.findViewById(R.id.tvDurationLabel)
      durationTotalTransactionsCost = view.findViewById(R.id.tvCost)
    }
  }

  // Create new views (invoked by the layout manager)
  override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
    // Create a new view, which defines the UI of the list item
    val view = LayoutInflater.from(viewGroup.context)
      .inflate(R.layout.cost_row_item, viewGroup, false)

    return ViewHolder(view)
  }

  // Replace the contents of a view (invoked by the layout manager)
  override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {

    // Get element from your dataset at this position and replace the
    // contents of the view with that element
    viewHolder.durationLabel.text = dataSet2[position]?.first
    viewHolder.durationTotalTransactionsCost.text = "Kshs %,.2f".format(dataSet2[position]?.second)
  }

  // Return the size of your dataset (invoked by the layout manager)
  override fun getItemCount() = dataSet2.size

}

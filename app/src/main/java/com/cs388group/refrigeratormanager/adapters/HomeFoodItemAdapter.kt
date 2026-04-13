package com.cs388group.refrigeratormanager.adapters
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.cs388group.refrigeratormanager.R
import com.cs388group.refrigeratormanager.data.HomeFoodItem
import com.cs388group.refrigeratormanager.databinding.ItemHomeFoodBinding
import com.cs388group.refrigeratormanager.fragments.FoodDetailFragment
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.TimeUnit

class HomeFoodItemAdapter : RecyclerView.Adapter<HomeFoodItemAdapter.HomeFoodViewHolder>() {

    private val items = mutableListOf<HomeFoodItem>()

    fun submitList(newItems: List<HomeFoodItem>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    fun getItemAt(position: Int): HomeFoodItem {
        return items[position]
    }

    fun removeItem(position: Int) {
        items.removeAt(position)
        notifyItemRemoved(position)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeFoodViewHolder {
        val binding = ItemHomeFoodBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return HomeFoodViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HomeFoodViewHolder, position: Int) {
        val item = items[position]
        holder.bind(item)
        holder.itemView.setOnClickListener {
            val context = holder.itemView.context
            val activity = context as? androidx.fragment.app.FragmentActivity ?: return@setOnClickListener

            val fragment = FoodDetailFragment().apply {
                arguments = Bundle().apply {
                    putString("foodName", item.itemName)
                    putString("foodLocation", item.locationName)
                    putString("quantity", item.quantity.toString())
                    putString("upc", item.catalogItemId)
                    putString("expirationDate", item.expirationDateText)
                    item.calories?.let { putInt("calories", it) }
                }
            }

            activity.supportFragmentManager.beginTransaction()
                .setCustomAnimations(
                    android.R.anim.slide_in_left,
                    android.R.anim.slide_out_right,
                    android.R.anim.slide_in_left,
                    android.R.anim.slide_out_right
                )
                .replace(R.id.main_frame_layout, fragment)
                .addToBackStack(null)
                .commit()
        }
    }

    override fun getItemCount(): Int = items.size

    class HomeFoodViewHolder(
        private val binding: ItemHomeFoodBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: HomeFoodItem) {
            binding.tvItemName.text = item.itemName
            binding.tvExpirationDate.text = "Expires: ${item.expirationDateText}"
            binding.tvLocation.text = "Location: ${item.locationName}"

            if (item.calories != null) {
                binding.tvCalories.visibility = View.VISIBLE
                binding.tvCalories.text = "Calories: ${item.calories} kcal"
            } else {
                binding.tvCalories.visibility = View.GONE
            }

            val daysLeft = calculateDaysRemaining(item.expirationDateText)
            when {
                daysLeft == null -> {
                    binding.tvDaysRemaining.text = "N/A"
                }
                daysLeft < 0 -> {
                    binding.tvDaysRemaining.text = "Expired"
                }
                daysLeft == 0 -> {
                    binding.tvDaysRemaining.text = "Today"
                }
                daysLeft == 1 -> {
                    binding.tvDaysRemaining.text = "1 day"
                }
                else -> {
                    binding.tvDaysRemaining.text = "$daysLeft days"
                }
            }

            when {
                daysLeft == null -> {
                    binding.tvDaysRemaining.setBackgroundResource(R.drawable.bg_left_days_gray)
                }
                daysLeft < 0 -> {
                    binding.tvDaysRemaining.setBackgroundResource(R.drawable.bg_left_days_red)
                }
                daysLeft <= 2 -> {
                    binding.tvDaysRemaining.setBackgroundResource(R.drawable.bg_left_days_orange)
                }
                else -> {
                    binding.tvDaysRemaining.setBackgroundResource(R.drawable.bg_left_days)
                }
            }
        }

        private fun calculateDaysRemaining(expirationText: String): Int? {
            return try {
                val cleaned = expirationText.removePrefix("Exp: ").trim()
                val formatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                formatter.isLenient = false

                val expirationDate = formatter.parse(cleaned) ?: return null

                val todayCal = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }

                val expirationCal = Calendar.getInstance().apply {
                    time = expirationDate
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }

                val diffMillis = expirationCal.timeInMillis - todayCal.timeInMillis
                TimeUnit.MILLISECONDS.toDays(diffMillis).toInt()
            } catch (e: Exception) {
                null
            }
        }
    }
}

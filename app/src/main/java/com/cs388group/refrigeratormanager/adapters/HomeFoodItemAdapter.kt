package com.cs388group.refrigeratormanager.adapters
    import android.os.Bundle
    import android.view.LayoutInflater
    import android.view.ViewGroup
    import androidx.recyclerview.widget.RecyclerView
    import com.cs388group.refrigeratormanager.R
    import com.cs388group.refrigeratormanager.data.HomeFoodItem
    import com.cs388group.refrigeratormanager.databinding.ItemHomeFoodBinding
    import com.cs388group.refrigeratormanager.fragments.FoodDetailFragment

class HomeFoodItemAdapter : RecyclerView.Adapter<HomeFoodItemAdapter.HomeFoodViewHolder>() {

        private val items = mutableListOf<HomeFoodItem>()

        fun submitList(newItems: List<HomeFoodItem>) {
            items.clear()
            items.addAll(newItems)
            notifyDataSetChanged()
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
            holder.bind(items[position])
            var item = items[position]
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


            }


        }
    }

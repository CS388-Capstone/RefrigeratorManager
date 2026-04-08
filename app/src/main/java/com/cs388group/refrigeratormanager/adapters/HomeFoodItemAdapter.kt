package com.cs388group.refrigeratormanager.adapters
    import android.view.LayoutInflater
    import android.view.ViewGroup
    import androidx.recyclerview.widget.RecyclerView
    import com.cs388group.refrigeratormanager.data.HomeFoodItem
    import com.cs388group.refrigeratormanager.databinding.ItemHomeFoodBinding

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

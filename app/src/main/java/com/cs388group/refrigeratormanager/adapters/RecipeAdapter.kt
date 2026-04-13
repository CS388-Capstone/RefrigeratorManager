package com.cs388group.refrigeratormanager.adapters

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.recyclerview.widget.RecyclerView
import com.cs388group.refrigeratormanager.R
import com.cs388group.refrigeratormanager.data.Recipe
import com.cs388group.refrigeratormanager.fragments.RecipeDetailFragment

class RecipeAdapter(private val recipes: List<Recipe>, private val onFavoriteClick: (Recipe, Boolean) -> Unit) :
    RecyclerView.Adapter<RecipeAdapter.RecipeViewHolder>() {

    class RecipeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val recipeImage: ImageView = itemView.findViewById(R.id.recipeImage)
        val recipeName: TextView = itemView.findViewById(R.id.recipeName)
        val recipeCalories: TextView = itemView.findViewById(R.id.recipeCalories)

        val starButton: ImageView = itemView.findViewById(R.id.starButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecipeViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_recipe, parent, false)
        return RecipeViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecipeViewHolder, position: Int) {
        var recipe = recipes[position]

        holder.recipeName.text = recipe.name
        holder.recipeCalories.text = recipe.calories
        holder.recipeImage.setImageResource(recipe.imageResId)
        holder.starButton.isSelected = recipe.favorited

        holder.starButton.setOnClickListener {
            val newState = !recipe.favorited
            holder.starButton.isSelected = newState
            onFavoriteClick(recipe, newState)
        }
        holder.itemView.setOnClickListener {
            val context = holder.itemView.context


            val activity = context as? androidx.fragment.app.FragmentActivity ?: return@setOnClickListener

            val fragment = RecipeDetailFragment().apply {
                arguments = Bundle().apply {
                    putString("recipeName", recipe.name)
                    putString(
                        "ingredientsText",
                        recipe.ingredients.entries.joinToString("\n") {
                            "${it.key}: ${it.value}"
                        }
                    )
                    putString(
                        "stepsText",
                        recipe.steps.mapIndexed { index, step ->
                            "${index + 1}. $step"
                        }.joinToString("\n")
                    )
                }
            }

            activity.supportFragmentManager.beginTransaction().setCustomAnimations(
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

    override fun getItemCount(): Int = recipes.size


}
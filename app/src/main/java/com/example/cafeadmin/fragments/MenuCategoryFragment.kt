package com.example.cafeadmin.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.cafeadmin.R
import com.example.cafeadmin.databinding.FragmentMenuCategoryBinding
import com.google.android.material.tabs.TabLayoutMediator

class MenuCategoryFragment : Fragment() {

    private var _binding: FragmentMenuCategoryBinding? = null
    private val binding get() = _binding!!

    private val categoryList = listOf(
        R.string.ichimliklar,
        R.string.fast_food,
        R.string.quyuq_ovqatlar,
        R.string.suyuq_ovqatlar
    )

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentMenuCategoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = object : FragmentStateAdapter(this) {
            override fun getItemCount(): Int = categoryList.size
            override fun createFragment(position: Int): Fragment {
                val category = getString(categoryList[position])
                return MenuFragment.newInstance(category)
            }
        }

        binding.viewPager.adapter = adapter

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = getString(categoryList[position])
        }.attach()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

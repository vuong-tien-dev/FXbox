package com.vtstudio.fxbox.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.vtstudio.fxbox.adapters.HomeItemAdapter
import com.vtstudio.fxbox.adapters.MediaItemAdapter
import com.vtstudio.fxbox.database.FxRoomDB
import com.vtstudio.fxbox.databinding.FragmentHomeBinding
import com.vtstudio.fxbox.media.models.FxMediaVideo
import com.vtstudio.fxbox.media.utils.ModelUtils

/**
 * A simple [Fragment] subclass.
 * Use the [Home.newInstance] factory method to
 * create an instance of this fragment.
 */
class Home : FxBaseFragment() {

    private var _homeBinding: FragmentHomeBinding? = null
    private val binding get() = _homeBinding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    var playerTopFragment: YoutubePlayerTop? = null
    var playerBottomFragment: YoutubeBottomFragment? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _homeBinding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val db = FxRoomDB.get(context)
        val videos = ModelUtils.fromYTDetailsList(db.ytvDetailsDao().allDetails)
        binding.homeFeedItems.adapter = HomeItemAdapter(videos)
        binding.homeFeedItems.layoutManager = LinearLayoutManager(context)
    }

    override fun onDestroyView() {
        _homeBinding = null
        super.onDestroyView()
    }

    override fun onDestroy() {

        super.onDestroy()
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @return A new instance of fragment Home.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(): Home {
            return Home()
        }
    }
}
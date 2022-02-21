package de.tjarksaul.wachmanager.modules.stationCheck

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import de.tjarksaul.wachmanager.R
import de.tjarksaul.wachmanager.api.HTTPRepo
import de.tjarksaul.wachmanager.dtos.Entry
import de.tjarksaul.wachmanager.dtos.Field
import de.tjarksaul.wachmanager.dtos.IdResponse
import de.tjarksaul.wachmanager.dtos.StateKind
import de.tjarksaul.wachmanager.modules.base.BaseFragment
import org.koin.android.ext.android.inject
import org.koin.standalone.KoinComponent
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class StationCheckFragment : BaseFragment(), KoinComponent {

    private val httpRepo = HTTPRepo()

    private val entryService: EntryService by inject()

    private val fieldCallback = object : Callback<MutableList<Field>> {
        override fun onFailure(call: Call<MutableList<Field>>?, t: Throwable?) {
            Log.e("MainActivity", "Problem calling Github API {${t?.message}}")
            showInternetConnectionError()
        }

        override fun onResponse(
            call: Call<MutableList<Field>>?,
            response: Response<MutableList<Field>>?
        ) {
            response?.isSuccessful.let {
                val data = response?.body() ?: emptyList<Field>().toMutableList()
                stationCheckViewModel.updateData(data)

                httpRepo.getEntries(getStationId(), entryCallback)
            }
        }
    }

    private val entryCallback = object : Callback<MutableList<Entry>> {
        override fun onFailure(call: Call<MutableList<Entry>>?, t: Throwable?) {
            Log.e("MainActivity", "Problem calling Github API {${t?.message}}")
            showInternetConnectionError()
        }

        override fun onResponse(
            call: Call<MutableList<Entry>>?,
            response: Response<MutableList<Entry>>?
        ) {
            response?.isSuccessful.let {
                val data = response?.body() ?: emptyList<Entry>().toMutableList()
                stationCheckViewModel.addEntries(data)
            }
        }
    }

    private val updateCallback = object : Callback<IdResponse> {
        override fun onFailure(call: Call<IdResponse>?, t: Throwable?) {
            Log.e("MainActivity", "Problem calling Github API {${t?.message}}")
            showInternetConnectionError()
        }

        override fun onResponse(call: Call<IdResponse>?, response: Response<IdResponse>?) {
            response?.isSuccessful.let {
                val data = response?.body()
                // todo: do something that makes sense
//                if (data != null) {
//                    stationCheckViewModel.updateValue(data.field, data.state ?: true, data.stateKind, data.amount, data.note)
//                }
            }
        }
    }


    val stationCheckViewModel: StationCheckViewModel by viewModels()

    override fun onSaveInstanceState(outState: Bundle) {
        stationCheckViewModel.saveState()

        super.onSaveInstanceState(outState)
    }

    private lateinit var listView: ListView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        if (stationCheckViewModel.needsRefresh()) {
            val stationId = getStoredStationId()
            if ((stationId != null) && isNetworkConnected()) {
                httpRepo.getFields(stationId, fieldCallback)
            } else {
                showInternetConnectionError()
            }
        }

        val root = inflater.inflate(R.layout.fragment_station_check, container, false)
        listView = root.findViewById(R.id.stationCheckList)
        stationCheckViewModel.entries.observe(viewLifecycleOwner, Observer {
            val adapter = activity?.applicationContext?.let { it1 ->
                StationCheckListAdapter(
                    it1,
                    it,
                    itemClickCallback = fun(
                        id: String,
                        state: Boolean,
                        stateKind: StateKind?,
                        amount: Int?,
                        note: String?
                    ) {
                        val index = listView.firstVisiblePosition
                        val v = listView.getChildAt(0)
                        val top = v?.top ?: 0
                        val stationId = getStoredStationId()

                        if (stationId !== null && (state || stateKind !== null)) {
                            entryService.updateEntry(
                                id,
                                stationId,
                                state,
                                stateKind,
                                amount,
                                note,
                                getCrew()
                            )
                        }

                        stationCheckViewModel.updateValue(id, state, stateKind, amount, note)

                        listView.setSelectionFromTop(index, top)
                    },
                    parentActivity = requireActivity()
                )
            }
            listView.adapter = adapter
        })
        return root
    }
}
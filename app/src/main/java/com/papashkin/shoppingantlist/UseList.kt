package com.papashkin.shoppingantlist

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.TextView
import java.io.File
import kotlin.collections.ArrayList

@Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
class UseList: Activity() {
    lateinit var tv_listName: TextView
    lateinit var lv_neededItems: ListView
    lateinit var lv_checkedItems: ListView
    lateinit var list_neededItems: ArrayList<String>
    lateinit var list_chekedItems: ArrayList<String>
    lateinit var fileName: String
    lateinit var neededAdapter: ArrayAdapter<String>
    lateinit var checkedAdapter: ArrayAdapter<String>
    lateinit var neededtouchListener: SwipeDismissListViewTouchListener
    lateinit var checkedtouchListener: SwipeDismissListViewTouchListener

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_uselist)

        if (savedInstanceState != null) {
            val isFromWidget = savedInstanceState.getBoolean("FROM_WIDGET",
                    false)
            if (isFromWidget) {
                val file = File(this.filesDir, fileName)
                file.useLines { lines ->
                    lines.forEach {
                        if (it != "") list_neededItems.add(it)
                    }
                }
                list_chekedItems = arrayListOf()
                fileName = savedInstanceState.getString("LIST_NAME")
            } else {
                fileName = intent.getStringExtra("LIST_NAME")
                list_neededItems = savedInstanceState.getStringArrayList("needed_items")
                list_chekedItems = savedInstanceState.getStringArrayList("checked_items")
            }
        } else {
            fileName = intent.getStringExtra("LIST_NAME")
            list_neededItems = intent.getStringArrayListExtra("LIST")
            list_chekedItems = arrayListOf()
        }

        tv_listName = this.findViewById(R.id.uselist_listname)
        tv_listName.text = fileName

        lv_neededItems = this.findViewById(R.id.lv_neededItem)
        neededtouchListener = SwipeDismissListViewTouchListener(lv_neededItems,
                object : SwipeDismissListViewTouchListener.DismissCallbacks {
                    override fun canDismiss(position: Int): Boolean {
                        return true
                    }

                    override fun onDismiss(listView: ListView, reverseSortedPositions: IntArray) {
                        for (position in reverseSortedPositions) {
                            itemsChange(position, neededAdapter, checkedAdapter)
                        }
                        setChanged(neededAdapter, checkedAdapter)
                    }
                })
        lv_neededItems.setOnTouchListener(neededtouchListener)
        lv_neededItems.setOnScrollListener(neededtouchListener.makeScrollListener())

        lv_checkedItems = this.findViewById(R.id.lv_checkedItem)
        checkedtouchListener = SwipeDismissListViewTouchListener(lv_checkedItems,
                object : SwipeDismissListViewTouchListener.DismissCallbacks {
                    override fun canDismiss(position: Int): Boolean {
                        return true
                    }

                    override fun onDismiss(listView: ListView, reverseSortedPositions: IntArray) {
                        for (position in reverseSortedPositions) {
                            itemsChange(position, checkedAdapter, neededAdapter)
                        }
                        setChanged(neededAdapter, checkedAdapter)
                    }
                })
        lv_checkedItems.setOnTouchListener(checkedtouchListener)
        lv_checkedItems.setOnScrollListener(checkedtouchListener.makeScrollListener())

        neededAdapter = ArrayAdapter(this, R.layout.textview_neededitems,
                list_neededItems)
        lv_neededItems.adapter = neededAdapter

        checkedAdapter = ArrayAdapter(this, R.layout.textview_checkeditems,
                list_chekedItems)
        lv_checkedItems.adapter = checkedAdapter
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        outState!!.putStringArrayList("needed_items", list_neededItems)
        outState.putStringArrayList("checked_items", list_chekedItems)
    }


    fun returnToMain(v: View){
        val intent = Intent(this@UseList, MainActivity::class.java)
        finish()
        startActivity(intent)
    }

    private fun setChanged(vararg adapters: ArrayAdapter<String>){
        adapters.forEach {
            it.notifyDataSetChanged()
        }
    }

    private fun itemsChange(position: Int, adapterFrom: ArrayAdapter<String>,
                            adapterTo: ArrayAdapter<String>){
        val item = adapterFrom.getItem(position)
        adapterFrom.remove(adapterFrom.getItem(position))
        adapterTo.add(item)
    }

}
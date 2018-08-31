package com.papashkin.shoppingantlist

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.*
import android.view.inputmethod.EditorInfo
import android.widget.*
import java.io.File
import java.util.*
import android.widget.Toast

class SetList: Activity() {
    lateinit var MSG_ITEM_ALREADY_IN_LIST: String
    lateinit var MSG_WRITE_SUCCESS: String
    lateinit var mAdapter:ArrayAdapter<String>
    lateinit var itemsList: LinkedList<String>
    lateinit var list_view: ListView
    lateinit var listName: TextView
    lateinit var text_newItem: EditText
    lateinit var saveBtn: ImageButton
    lateinit var name: String
    lateinit var touchListener: SwipeDismissListViewTouchListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setlist)

        MSG_ITEM_ALREADY_IN_LIST = resources.getString(R.string.already_in_list)
        MSG_WRITE_SUCCESS = resources.getString(R.string.list_write_success)

        name = intent.getStringExtra("LIST_NAME")
        itemsList = LinkedList()
        val test = intent.getStringArrayListExtra("LIST")
        if (test.isNotEmpty()) {
            test.forEach {
                itemsList.add(it)
            }
        }

        saveBtn = this.findViewById(R.id.btn_save_setlist)
        list_view = this.findViewById(R.id.newlist_itemsList)
        listName = this.findViewById(R.id.newlist_listName)
        listName.text = name

        text_newItem = findViewById(R.id.newlist_text_newitem)
        text_newItem.setOnEditorActionListener { v, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                addItem(v)
                true
            } else {
                false
            }
        }

        touchListener = SwipeDismissListViewTouchListener(
                list_view,
                object : SwipeDismissListViewTouchListener.DismissCallbacks {
                    override fun canDismiss(position: Int): Boolean {
                        return true
                    }

                    override fun onDismiss(listView: ListView, reverseSortedPositions: IntArray) {
                        for (position in reverseSortedPositions) {
                            mAdapter.remove(mAdapter.getItem(position))
                        }
                        if (saveBtn.visibility == View.INVISIBLE){
                            saveBtn.visibility = View.VISIBLE
                        }
                        mAdapter.notifyDataSetChanged()
                    }
                })
        list_view.setOnTouchListener(touchListener)
        list_view.setOnScrollListener(touchListener.makeScrollListener())

        mAdapter = ArrayAdapter(this, R.layout.textview_item, itemsList)
        list_view.adapter = mAdapter
    }

    fun addItem(v: View) {
        val str: String
        if (text_newItem.text.isNotEmpty()){
            str = text_newItem.text.toString()
            if (itemsList.contains(str)){
                (Toast.makeText(this, MSG_ITEM_ALREADY_IN_LIST,
                        Toast.LENGTH_SHORT)).show()
            } else {
                mAdapter.add(str)
                mAdapter.notifyDataSetChanged()
            }
            if (saveBtn.visibility == View.INVISIBLE){
                saveBtn.visibility = View.VISIBLE
            }
            text_newItem.text.clear()
        }
    }

    fun saveList(v: View) {
        val dir = filesDir
        if (File(dir, "$name.txt").isFile){
            overwriteFile()
        } else {
            saveFile()
        }
    }

    fun getBack(v: View){
        val listFromFile = arrayListOf<String>()
        val file = File(filesDir,"$name.txt")
        if (file.isFile){
            file.useLines { lines -> lines.forEach{
                if (it != "") listFromFile.add(it)
            }
            }
        }
        val areEquals = (listFromFile.containsAll(itemsList)) &&
                (itemsList.containsAll(listFromFile))
        if (!areEquals){
            val dialog = Dialog(this@SetList)
            dialog.setContentView(R.layout.dialog_update_list)
            val btnOk = dialog.findViewById<Button>(R.id.ok_button)
            val btnCancel = dialog.findViewById<Button>(R.id.cancel_button)

            btnOk.setOnClickListener {
                dialog.dismiss()
                overwriteFile()
                exit()
            }
            btnCancel.setOnClickListener {
                dialog.dismiss()
                exit()
            }
            dialog.show()
        } else {
            exit()
        }
    }

    fun sentToWeb(v:View){
        var message = "to-do list \"${listName.text.toString()}\":\n"
        for (i in itemsList.indices){
            message += ("$i. ${itemsList[i]};\n")
        }
        val sendIntent = Intent()
        sendIntent.action = Intent.ACTION_SEND
        sendIntent.putExtra(Intent.EXTRA_TEXT, message)
        sendIntent.type = "text/plain"
        startActivity(sendIntent)
    }

    private fun exit(){
        val intent = Intent(this@SetList, MainActivity::class.java)
        finish()
        startActivity(intent)
    }

    private fun overwriteFile(){
        deleteFile("$name.txt")
        saveFile()
    }

    private fun saveFile(){
        var str = ""
        val newFile = openFileOutput("$name.txt",
                Context.MODE_PRIVATE)
        for (i in itemsList.indices){
            str += if (i == itemsList.lastIndex) {
                itemsList[i]
            } else {
                itemsList[i] + "\n"
            }
        }
        newFile.write(str.toByteArray())
        newFile.close()
        (Toast.makeText(this, MSG_WRITE_SUCCESS, Toast.LENGTH_SHORT)).show()
        saveBtn.visibility = View.INVISIBLE
    }
}
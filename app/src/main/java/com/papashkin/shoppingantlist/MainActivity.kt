package com.papashkin.shoppingantlist

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.*
import java.io.*
import java.util.*
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity() {
    private val LIST_MODIFY = 1

    lateinit var MSG_NEED_ITEM_NAME: String
    lateinit var MSG_TITLE: String
    lateinit var MSG_LOAD2USE: String
    lateinit var MSG_LOAD2MODIFY: String
    lateinit var MSG_DELETE: String
    lateinit var listsNames: LinkedList<String>
    lateinit var mAdapter: ArrayAdapter<String>
    lateinit var mList: ListView
    lateinit var inputText: EditText
    lateinit var btn_OK: Button
    lateinit var btn_Cancel: Button
    lateinit var appFilesDir: File

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        title = resources.getString(R.string.main_title)
        MSG_TITLE = resources.getString(R.string.new_list_name)
        appFilesDir = this.filesDir
        mList = findViewById(R.id.layout_lists)
        mList.setOnItemClickListener {_, view, position, _ ->
            showPopupMenu(view, listsNames[position])
        }
        checkFilesList()
    }

    override fun onResume(){
        super.onResume()
        checkFilesList()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when (item!!.itemId) {
            R.id.mainmenu_newlist -> {
                createNewList()
                true
            }
            R.id.mainmenu_exit -> {
                finish()
                true
            }
            else -> false
        }
    }

    private fun createNewList(){
        MSG_NEED_ITEM_NAME = resources.getString(R.string.need_item_name)
        val dialog = Dialog(this@MainActivity)
        dialog.setContentView(R.layout.dialog_new_item)
        dialog.setTitle(MSG_TITLE)
        inputText = dialog.findViewById(R.id.text_item_name)
        inputText.setOnEditorActionListener { v, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                newList()
                true
            } else {
                false
            }
        }
        btn_OK = dialog.findViewById(R.id.ok_button)
        btn_Cancel = dialog.findViewById(R.id.cancel_button)

        btn_OK.setOnClickListener {
            if (inputText.editableText.toString() == ""){
                (Toast.makeText(this, MSG_NEED_ITEM_NAME,
                        Toast.LENGTH_SHORT)).show()
            } else {
                dialog.dismiss()
                newList()
            }
        }
        btn_Cancel.setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun newList(){
        val str = inputText.editableText.toString()
        listsNames.add(str)
        val newListIntent = Intent(this@MainActivity, SetList::class.java)
        startNewListActivity(newListIntent, arrayListOf(),str)
    }

    private fun showPopupMenu(view: View, fileName: String){
        val popupMenu = PopupMenu(this, view)
        view.setBackgroundColor(Color.CYAN)
        popupMenu.inflate(R.menu.popupmenu_main)
        val file = "$fileName.txt"

        popupMenu.setOnMenuItemClickListener(object : PopupMenu.OnMenuItemClickListener {
            override fun onMenuItemClick(item: MenuItem) : Boolean {
                MSG_LOAD2USE = resources.getString(R.string.load_to_use, fileName)
                MSG_LOAD2MODIFY = resources.getString(R.string.load_to_modify, fileName)
                MSG_DELETE = resources.getString(R.string.delete_list, fileName)
                view.setBackgroundColor(Color.WHITE)
                return when(item.itemId){
                    R.id.popup_menu_modifyList -> {
                        Toast.makeText(applicationContext, MSG_LOAD2MODIFY,
                                Toast.LENGTH_SHORT).show()
                        loadList(file, LIST_MODIFY)
                        true
                    }
                    R.id.popup_menu_loadList -> {
                        Toast.makeText(applicationContext, MSG_LOAD2USE,
                                Toast.LENGTH_SHORT).show()
                        loadList(file, 0)
                        true
                    }
                    R.id.popup_menu_deleteList -> {
                        deleteList(fileName)
                        true
                    }
                    else -> false
                }
            }
        })

        popupMenu.setOnDismissListener {
            view.setBackgroundColor(Color.WHITE)
            it.dismiss()
        }

        popupMenu.show()
    }

    private fun deleteList(fileName: String){
        val dialog = Dialog(this@MainActivity)
        dialog.setContentView(R.layout.dialog_delete_list)
        val btnOk = dialog.findViewById<Button>(R.id.ok_button)
        val btnCancel = dialog.findViewById<Button>(R.id.cancel_button)

        btnOk.setOnClickListener {
            dialog.dismiss()
            deleteFile("$fileName.txt")
            listsNames.remove(fileName)
            mList.adapter = mAdapter
            Toast.makeText(applicationContext, MSG_DELETE,
                    Toast.LENGTH_SHORT).show()
        }
        btnCancel.setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun loadList(fileName: String, id: Int) {
        val list = arrayListOf<String>()
        val file = File(appFilesDir, fileName)
        file.useLines { lines -> lines.forEach{
            if (it != "") list.add(it)
        }
        }

        val listIntent = when (id){
            1 -> Intent(this@MainActivity, SetList::class.java)
            else -> Intent(this@MainActivity, UseList::class.java)
        }
        startNewListActivity(listIntent, list, file.nameWithoutExtension)
    }

    private fun startNewListActivity(intent: Intent, list: ArrayList<String>, fileName: String){
        intent.putStringArrayListExtra("LIST", list)
        intent.putExtra("LIST_NAME", fileName)
        finish()
        startActivity(intent)
    }

    private fun checkFilesList(){
        listsNames = LinkedList()
        val dir = appFilesDir.listFiles()
        if (dir.isNotEmpty()){
            for (i in dir.indices){
                listsNames.add(dir[i].nameWithoutExtension)
            }
        }
        mAdapter = ArrayAdapter(this, R.layout.textview_list, listsNames)
        mList.adapter = mAdapter
    }
}
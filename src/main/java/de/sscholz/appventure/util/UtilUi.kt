package de.sscholz.appventure.util

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.util.DisplayMetrics
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import com.google.android.material.snackbar.Snackbar

fun <T> LiveData<T>.getOrDefault(defaultValue: T) = this.value ?: defaultValue

fun View.hideKeyboard() {
    val imm = this.context!!.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(this.windowToken, 0) // hide keyboard
}

fun Activity.toast(notificationText: String) {
    Toast.makeText(this, notificationText, Toast.LENGTH_SHORT).show()
}

fun View.snackbar(notificationText: String) {
    Snackbar.make(this, notificationText, Snackbar.LENGTH_SHORT).show()
}

fun Fragment.snackbar(notificationText: String) {
    activity!!.snackbar(notificationText)
}

fun Activity.snackbar(notificationText: String) {
    findViewById<View>(android.R.id.content).snackbar(notificationText)
}

fun Context.displaySimpleAlert(message: String, buttonTitle: String = "OK") {
    val dialog = AlertDialog.Builder(this).setTitle(null).setMessage(message)
            .setPositiveButton(buttonTitle, null)
    dialog.show()
}

fun Fragment.displaySimpleAlert(message: String, buttonTitle: String = "OK") {
    context!!.displaySimpleAlert(message, buttonTitle)
}

fun EditText.getMyText(): String {
    return this.text.toString()
}

fun EditText.setMyText(newText: String) {
    this.text.clear()
    this.text.append(newText)
}

fun Context.copyTextToClipboard(textToBeCopied: String) {
    val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText("appVentureClip", textToBeCopied)
    clipboard.primaryClip = clip
}

val Activity.screenWidth: Int
    get() {
        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        return displayMetrics.widthPixels
    }

val Activity.screenHeight: Int
    get() {
        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        return displayMetrics.heightPixels
    }

fun <T> LiveData<T>.observeOnce(observer: Observer<T>) {
    observeForever(object : Observer<T> {
        override fun onChanged(t: T?) {
            removeObserver(this)
            observer.onChanged(t)
        }
    })
}

// normal observe, but with kotlin lambda instead observer object
fun <T> LiveData<T>.observe(owner: LifecycleOwner, observer: (t: T?) -> Unit) {
    this.observe(owner, Observer { observer(it) })
}

// only calls observers for non-null values
fun <T> LiveData<T>.nonNullObserve(owner: LifecycleOwner, observer: (t: T) -> Unit) {
    this.observe(owner, Observer { it?.let(observer) })
}

fun <T> LiveData<T>.nonNullObserveOnce(observer: (t: T) -> Unit) {
    observeForever(object : Observer<T> {
        override fun onChanged(t: T?) {
            if (t != null) {
                removeObserver(this)
                observer(t)
            }
        }
    })
}
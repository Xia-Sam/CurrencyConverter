import android.content.Context
import android.widget.Toast

class Utils {
    companion object {
        fun showToast(context: Context, m: String) {
            Toast.makeText(context, m, Toast.LENGTH_SHORT).show()
        }
    }
}
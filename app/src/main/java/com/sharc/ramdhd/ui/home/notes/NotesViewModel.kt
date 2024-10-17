import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.sharc.ramdhd.data.database.AppDatabase
import com.sharc.ramdhd.data.model.Note
import com.sharc.ramdhd.data.repository.NoteRepository
import kotlinx.coroutines.flow.Flow

class NotesViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: NoteRepository
    val allNotes: Flow<List<Note>>

    init {
        val noteDao = AppDatabase.getDatabase(application).noteDao()
        repository = NoteRepository(noteDao)
        allNotes = repository.allNotes
    }
}
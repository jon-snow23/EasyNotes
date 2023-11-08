package com.shiva.easynotes.Fragments

import android.animation.Animator.AnimatorListener
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.transition.MaterialContainerTransform
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.shiva.easynotes.Models.NoteModel
import com.shiva.easynotes.R
import com.shiva.easynotes.Utils.hideKeyboard
import com.shiva.easynotes.databinding.FragmentSaveEditBinding
import com.thebluealliance.spectrum.SpectrumPalette
import com.yahiaangelo.markdownedittext.MarkdownEditText
import com.yahiaangelo.markdownedittext.MarkdownStylesBar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.jar.Manifest


class SaveEditFragment : Fragment() {

    private val REQUEST_AUDIO_PERMISSION = 123
    private lateinit var noteContentFragmentParent: RelativeLayout
    private lateinit var toolbarFragmentNoteContent:RelativeLayout
    private lateinit var navController: NavController
    private var _binding: FragmentSaveEditBinding? = null
    private lateinit var speechRecognizer: SpeechRecognizer
    private lateinit var speechIntent : Intent
    private val binding get()=_binding!!
    private val data= NoteModel()
    private var note: NoteModel? = null
    private lateinit var lastEdited: TextView
    private lateinit var saveNote: ImageView
    var etTitle: EditText? = null
    lateinit var etNoteContent: MarkdownEditText
    private lateinit var fabColorPick: FloatingActionButton
    private lateinit var bottomBar: LinearLayout
    private lateinit var styleBar: MarkdownStylesBar
    private var color:Int = -1315861
    private var noteId: String? = null
    private val expandAnim by lazy { AnimationUtils.loadAnimation(requireContext(), R.anim.expand_horizontal)}
    private var mContext: Context? = null
    private var isListening = false
    @SuppressLint("ResourceAsColor")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val animation = MaterialContainerTransform().apply {
            drawingViewId= R.id.fragments
            scrimColor= R.color.transparent
            duration=300L

        }
        sharedElementEnterTransition=animation
        sharedElementReturnTransition=animation
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val callback= object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (etNoteContent.text.toString().isEmpty() &&
                    etTitle?.text.toString().isEmpty() && noteId==null){
                    Navigation.findNavController(view!!).navigate(R.id.action_saveEditFragment_to_noteFragment)
                }
                else {
                    saveNote()
                }
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner,callback)
        _binding = FragmentSaveEditBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        noteContentFragmentParent=view.findViewById(R.id.noteContentFragmentParent)
        toolbarFragmentNoteContent=view.findViewById(R.id.toolbarFragmentNoteContent)
        navController=Navigation.findNavController(view)
        lastEdited=view.findViewById(R.id.last_edited)
        saveNote=view.findViewById(R.id.save_note)
        etTitle=view.findViewById(R.id.et_title)
        etNoteContent=view.findViewById(R.id.et_noteContent)
        fabColorPick=view.findViewById(R.id.fab_color_pick)
        bottomBar=view.findViewById(R.id.bottom_bar)
        styleBar=view.findViewById(R.id.style_bar)
        noteId=arguments?.getString("noteId")

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(requireContext())
        speechIntent = Intent (RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        speechIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        speechIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())

        speechRecognizer.setRecognitionListener(object: RecognitionListener{
            override fun onReadyForSpeech(params: Bundle?) {}
            override fun onPartialResults(partialResults: Bundle?) {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {}
            override fun onError(error: Int) {}
            override fun onResults(results: Bundle?) {
                results?.let{result->
                    val data = result.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    Log.w("notes-result", "reached data $data ${binding.etNoteContent.isFocused} ${binding.etTitle.isFocused}")

                    if(binding.etTitle.isFocused){
                        var updatedText = binding.etTitle.text.toString()
                        updatedText.trim()
                        updatedText+=" ${data.toString().substring(1, data.toString().length-1)}"
                        Log.w("notes-result", "$updatedText")
                        binding.etTitle.setText(updatedText)
                    }
                    else{
                        var updatedText = binding.etNoteContent.text.toString()
                        updatedText.trim()
                        updatedText+=" ${data.toString().substring(1, data.toString().length-1)}"
                        binding.etNoteContent.setText(updatedText)
                    }
                }
            }
        })

        CoroutineScope(Dispatchers.Main).launch {
            delay(10)
            ViewCompat.setTransitionName(
                noteContentFragmentParent,
                "recyclerView_${note?.id}"
            )
        }
        requireView().hideKeyboard()
        setUpNote()
        etNoteContent.setStylesBar(styleBar)
        lastEdited.text=getString(R.string.edited_on, SimpleDateFormat.getInstance().format(Date()))
        try {
            etNoteContent.setOnFocusChangeListener { _, hasFocus ->
                if (hasFocus) {
                    bottomBar.visibility = View.VISIBLE
                } else {
                    bottomBar.visibility = View.GONE
                }
            }
        } catch (e: Throwable) {
            Log.d("TAG", e.stackTraceToString())
        }
        fabColorPick.setOnClickListener {
            val bottomSheetDialog= BottomSheetDialog(
                requireContext(),
                R.style.BottomSheetDialogTheme
            )
            val bottomSheetView:View=layoutInflater.inflate(
                R.layout.bottom_sheet_layout,
                null
            )
            with(bottomSheetDialog){
                setContentView(bottomSheetView)
                show()
            }
            val colorPicker=bottomSheetDialog.findViewById<SpectrumPalette>(R.id.colorPicker)
            val bottomSheetParent= bottomSheetDialog.findViewById<CardView>(R.id.bottomSheetParent)
            colorPicker?.apply {
                setSelectedColor(color)
                bottomSheetParent?.setCardBackgroundColor(color)
                setOnColorSelectedListener {value->
                    color=value
                    noteContentFragmentParent.setBackgroundColor(color)
                    toolbarFragmentNoteContent.setBackgroundColor(color)
                    bottomBar.setBackgroundColor(color)
                    activity?.window?.statusBarColor=color
                    bottomSheetParent?.setCardBackgroundColor(color)
                    activity?.window?.navigationBarColor=color
                }
            }
            bottomSheetView.post{
                bottomSheetDialog.behavior.state= BottomSheetBehavior.STATE_EXPANDED
            }
        }
        saveNote.setOnClickListener {
            saveNote()
        }
        expandAnim.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation) {}

            override fun onAnimationEnd(animation: Animation) {
                binding.record.cardText.visibility = View.VISIBLE
                binding.record.cardIcon.setImageDrawable(resources.getDrawable(R.drawable.baseline_stop_24))
                binding.record.recordBtn.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.card_red))
            }

            override fun onAnimationRepeat(animation: Animation) {}
        })
        binding.record.recordBtn.setOnClickListener{
            if(isListening){
                speechRecognizer.stopListening()
                binding.record.cardText.visibility = View.GONE
                binding.record.cardIcon.setImageDrawable(resources.getDrawable(R.drawable.ic_mic))
                binding.record.recordBtn.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.purple))
            }
            else{
                if(!hasAudioPermission()){
                    askPermission()
                    isListening=!isListening
                }
                else{
                    binding.record.recordBtn.startAnimation(expandAnim)
                    speechRecognizer.startListening(speechIntent)
                }
            }
            isListening=!isListening
        }
    }
    private fun hasAudioPermission():Boolean{
        if(ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED){
            return false
        }
        return true
    }
    private fun askPermission(){
        ActivityCompat.requestPermissions(requireActivity(), arrayOf(android.Manifest.permission.RECORD_AUDIO), REQUEST_AUDIO_PERMISSION)
    }



    private fun saveNote() {
        if (etNoteContent.text.toString().isEmpty() &&
            etTitle?.text.toString().isEmpty()
        ) {
            Toast.makeText(activity, "Something is Empty", Toast.LENGTH_SHORT).show()
        }
        else if (etTitle?.text.toString().isEmpty()){
            etTitle?.setText("Untitled Note")
            val documentReference: DocumentReference =
                FirebaseFirestore.getInstance().collection("notes").document(FirebaseAuth.getInstance().uid.toString())
                    .collection("myNotes").document()
            data.id=documentReference.id
            data.title= etTitle?.text.toString()
            data.content = etNoteContent.getMD()
            data.date = Date().time
            data.color = color
            when(noteId){
                null->{
                    documentReference.set(data).addOnSuccessListener {
                        Toast.makeText(requireContext(), "Note Saved", Toast.LENGTH_SHORT).show()
                        navController.navigate(R.id.action_saveEditFragment_to_noteFragment)
                    }
                }
                else->{
                    updateNote()
                }
            }
        }
        else {
            val documentReference: DocumentReference =
                FirebaseFirestore.getInstance().collection("notes").document(FirebaseAuth.getInstance().uid.toString())
                    .collection("myNotes").document()

            data.id=documentReference.id
            data.title= etTitle?.text.toString()
            data.content = etNoteContent.getMD()
            data.date = Date().time
            data.color = color
            when(noteId){
                null->{
                    documentReference.set(data).addOnSuccessListener {
                        Toast.makeText(requireContext(), "Note Saved", Toast.LENGTH_SHORT).show()
                        navController.navigate(R.id.action_saveEditFragment_to_noteFragment)
                    }
                }
                else->{
                    updateNote()
                }
            }
        }
    }

    private fun updateNote() {
            data.id=noteId!!
            data.title= etTitle?.text.toString()
            data.content = etNoteContent.getMD()
            data.date = Date().time
            data.color = color
            FirebaseFirestore.getInstance().collection("notes").document(FirebaseAuth.getInstance().uid.toString())
                .collection("myNotes").document(noteId!!).set(data).addOnSuccessListener {
                    navController=Navigation.findNavController(requireView())
                    navController.navigate(R.id.action_saveEditFragment_to_noteFragment)
                }
    }
    private fun setUpNote(){
        noteId=arguments?.getString("noteId")
        if (noteId==null){
            lastEdited.text=getString(R.string.edited_on,SimpleDateFormat.getInstance().format(Date()))
        }
        if (noteId!=null){
            FirebaseFirestore.getInstance().collection("notes").document(FirebaseAuth.getInstance().uid.toString())
                .collection("myNotes").document(noteId.toString()).addSnapshotListener { value, _ ->
                    val data = value?.toObject(NoteModel::class.java)
                    if (data != null) {
                        etTitle?.setText(data.title)
                        etNoteContent.renderMD(data.content)
                        lastEdited.text = getString(
                            R.string.edited_on,
                            SimpleDateFormat.getInstance().format(Date(data.date))
                        )
                        color = data.color
                        noteContentFragmentParent.setBackgroundColor(color)
                        toolbarFragmentNoteContent.setBackgroundColor(color)
                        bottomBar.setBackgroundColor(color)
                        activity?.window?.statusBarColor = color
                        activity?.window?.navigationBarColor = color
                    }
                }
        }
    }
    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    }
    override fun onDetach() {
        super.onDetach()
        mContext = null
    }
}
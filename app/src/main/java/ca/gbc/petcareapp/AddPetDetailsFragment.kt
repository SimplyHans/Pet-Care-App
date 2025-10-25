                    package ca.gbc.petcareapp

                    import android.os.Bundle
                    import android.view.LayoutInflater
                    import android.view.View
                    import android.view.ViewGroup
                    import android.widget.EditText
                    import androidx.fragment.app.Fragment
                    import androidx.lifecycle.ViewModelProvider
                    import androidx.navigation.fragment.findNavController
                    import ca.gbc.petcareapp.auth.data.AppDatabase
                    import ca.gbc.petcareapp.pets.PetViewModel
                    import ca.gbc.petcareapp.pets.PetViewModelFactory
                    import com.google.android.material.button.MaterialButton
                    import com.google.android.material.textfield.TextInputEditText

                    class AddPetDetailsFragment : Fragment() {
                        private lateinit var breedInput: TextInputEditText
                        private lateinit var ageInput: EditText
                        private lateinit var descInput: EditText
                        private lateinit var saveBtn: MaterialButton
                        private lateinit var backBtn: MaterialButton
                        private lateinit var viewModel: PetViewModel

                        override fun onCreateView(
                            inflater: LayoutInflater, container: ViewGroup?,
                            savedInstanceState: Bundle?
                        ): View? {
                            val view = inflater.inflate(R.layout.add_pet_details, container, false)

                            // Initialize Room + ViewModel
                            val db = AppDatabase.get(requireContext())
                            val factory = PetViewModelFactory(db)
                            viewModel = ViewModelProvider(requireActivity(), factory)[PetViewModel::class.java]

                            // Bind UI
                            breedInput = view.findViewById(R.id.breedInput)
                            ageInput = view.findViewById(R.id.ageInput)
                            descInput = view.findViewById(R.id.descInput)
                            saveBtn = view.findViewById(R.id.delPetBtn)
                            backBtn = view.findViewById(R.id.backBtn)

                            // Save pet details
                            saveBtn.setOnClickListener {
                                // Pet name is already in ViewModel from previous fragment
                                viewModel.breed = breedInput.text.toString()
                                viewModel.age = ageInput.text.toString().toIntOrNull() ?: 0
                                viewModel.desc = descInput.text.toString()

                                // TODO: replace with actual logged-in user ID
                                viewModel.savePet(userId = 1L)

                                findNavController().navigate(R.id.petListFragment)

                            }


                            // Back button
                            backBtn.setOnClickListener {
                                findNavController().navigateUp()
                            }

                            return view
                        }
                    }

$(function() {

	// User Registeration Validation
	var $userReigsteration = $("#userRegister");

	$userReigsteration.validate({
		rules: {
			name: {
				required: true,
				lettersonly: true
			}
			,
			email: {
				required: true,
				space: true,
				email: true
			},
			mobileNumber: {
				required: true,
				space: true,
				numericOnly: true,
				minlength: 10,
				maxlength: 12

			},
			password: {
				required: true,
				space: true

			},
			confirmpassword: {
				required: true,
				space: true,
				equalTo: '#pass'

			}

		},
		messages: {
			name: {
				required: 'Name required',
				lettersonly: 'Invalid name'
			},
			email: {
				required: 'Email must be required',
				space: 'Space not allowed',
				email: 'Invalid email'
			},
			mobileNumber: {
				required: 'Mobile No. must be required',
				space: 'Space not allowed',
				numericOnly: 'Invalid mobile no.',
				minlength: 'Min 10 digit',
				maxlength: 'Max 12 digit'
			},

			password: {
				required: 'Password must be required',
				space: 'Space not allowed'

			},
			confirmpassword: {
				required: 'Confirm password must be required',
				space: 'Space not allowed',
				equalTo: 'Password mismatch'

			}
		}


	})

	// Admin Registeration Validation
var $adminRegisteration = $("#adminRegister");

	$adminRegisteration.validate({
		rules: {
			name: {
				required: true,
				lettersonly: true
			}
			,
			email: {
				required: true,
				space: true,
				email: true
			},
			mobileNumber: {
				required: true,
				space: true,
				numericOnly: true,
				minlength: 10,
				maxlength: 12

			},
			password: {
				required: true,
				space: true

			},
			confirmpassword: {
				required: true,
				space: true,
				equalTo: '#pass'

			},
			img: {
				required: true,
			}

		},
		messages: {
			name: {
				required: 'Name required',
				lettersonly: 'Invalid name'
			},
			email: {
				required: 'Email must be required',
				space: 'Space not allowed',
				email: 'Invalid email'
			},
			mobileNumber: {
				required: 'Mobile no. must be required',
				space: 'Space not allowed',
				numericOnly: 'Invalid mobile no.',
				minlength: 'Min 10 digit',
				maxlength: 'Max 12 digit'
			},

			password: {
				required: 'Password must be required',
				space: 'Space not allowed'

			},
			confirmpassword: {
				required: 'Confirm password must be required',
				space: 'Space not allowed',
				equalTo: 'Password mismatch'

			},
			img: {
				required: 'Image required',
			}
		}


	})
})


Query.validator.addMethod('lettersonly', function(value, element) {
	return /^[^-\s][a-zA-Z_\s-]+$/.test(value);
});

jQuery.validator.addMethod('space', function(value, element) {
	return /^[^-\s]+$/.test(value);
});

jQuery.validator.addMethod('all', function(value, element) {
	return /^[^-\s][a-zA-Z0-9_,.\s-]+$/.test(value);
});

jQuery.validator.addMethod('numericOnly', function(value, element) {
	return /^[0-9]+$/.test(value);
});
import React, { useState, useContext } from 'react';
import { useFormik } from 'formik';
import * as Yup from 'yup';
import { AuthContext } from '../../contexts/AuthContext';
 
const AuthPopup = ({ onClose, onLoginSuccess }) => {
  const [isLogin, setIsLogin] = useState(true);
  const [submitted, setSubmitted] = useState(false);
  const [apiError, setApiError] = useState('');
  const { login } = useContext(AuthContext);
  const toggleMode = () => {
    setIsLogin(!isLogin);
    setSubmitted(false);
    setApiError('');
  };
  const getValidationSchema = () => {
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    const baseSchema = {
      email: Yup.string()
        .matches(emailRegex, 'Invalid email format')
        .required('Email is required'),
      password: Yup.string()
        .min(8, 'Password must be at least 8 characters')
        .matches(/[A-Z]/, 'Must include an uppercase letter')
        .matches(/[a-z]/, 'Must include a lowercase letter')
        .matches(/[0-9]/, 'Must include a number')
        .matches(/[@$!%*?&#]/, 'Must include a special character')
        .required('Password is required')
    };
    if (!isLogin) {
      return Yup.object({
        ...baseSchema,
        name: Yup.string()
          .min(2, 'Name must be at least 2 characters')
          .matches(/^[a-zA-Z\s]*$/, 'Name can only contain letters')
          .required('Name is required'),
        role: Yup.string()
          .min(2, 'Role must be at least 2 characters')
          .matches(/^[a-zA-Z\s]*$/, 'Role can only contain letters')
          .required('Role is required'),
        contact: Yup.string()
          .matches(/^[0-9]{10}$/, 'Contact must be 10 digits')
          .required('Contact is required'),
        confirmPassword: Yup.string()
          .oneOf([Yup.ref('password')], 'Passwords must match')
          .required('Confirm Password is required')
      });
    }
    return Yup.object(baseSchema);
  };
 
  const formik = useFormik({
    initialValues: {
      name: '',
      email: '',
      role: '',
      contact: '',
      password: '',
      confirmPassword: ''
    },
    validationSchema: getValidationSchema(),
    enableReinitialize: true,
    onSubmit: async (values, { setSubmitting }) => {
      setSubmitted(false);
      setApiError('');
      console.log(formik.values);
      if (isLogin) {
  try {
    const response = await fetch('http://localhost:8080/api/auth/login', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        email: values.email,
        password: values.password
      })
    });
 
    if (!response.ok) {
      const errorData = await response.json();
      setApiError(errorData.message || 'Login failed');
      setSubmitting(false);
      return;
    }
 
    const data = await response.json();
    if (data.token) {
      localStorage.setItem('auth_token', data.token);
    }
    if (data.user) {
      // Save full user object (including userId) in localStorage and context
      localStorage.setItem('user', JSON.stringify(data.user));
      login(data.user);
    }
    if (onLoginSuccess) onLoginSuccess();
    setSubmitted(true);
  } catch (error) {
    setApiError('Network error');
  }
 
  setSubmitting(false);
  return;
}
 
 
 
      // Registration: Send data to backend
      try {
        const response = await fetch('http://localhost:8080/api/auth/register', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({
            userName: values.name,
            email: values.email,
            password: values.password,
            role: values.role,
            contactNo: values.contact
          })
        });
        if (response.ok) {
          const data = await response.json();
          if (data.user) {
            localStorage.setItem('user', JSON.stringify(data.user));
            login(data.user);
          }
          setSubmitted(true);
          if (onLoginSuccess) onLoginSuccess();
        } else {
          const errorData = await response.json();
          setApiError(errorData.message || 'Registration failed');
        }
      } catch (error) {
        setApiError('Network error');
      }
      setSubmitting(false);
    }
  });
  // Inline style for scrollable modal content
  const modalContentStyle = {
    maxHeight: '90vh',
    overflowY: 'auto'
  };
  return (
<div className="fixed inset-0 z-50 flex items-center justify-center bg-black bg-opacity-60 p-4">
<div
        className="relative w-full max-w-md bg-white rounded-2xl shadow-xl p-8 transition-all duration-300"
        style={modalContentStyle}
>
        {/* Close Button */}
<button
          onClick={onClose}
          className="absolute top-3 right-3 text-gray-500 hover:text-gray-700 text-2xl font-bold focus:outline-none"
>
&times;
</button>
        {/* Header */}
<h2 className="text-2xl font-bold text-center text-indigo-600 mb-6">
          {isLogin ? 'Login to Your Account' : 'Create a New Account'}
</h2>
        {/* API Error Message */}
        {apiError && (
<div className="mb-4 p-3 text-red-800 bg-red-100 border border-red-300 rounded-md text-center text-sm">
            {apiError}
</div>
        )}
        {/* Success Message */}
        {submitted && (
<div className="mb-4 p-3 text-green-800 bg-green-100 border border-green-300 rounded-md text-center text-sm">
            {isLogin ? 'Login successful!' : 'Account created successfully!'}
</div>
        )}
        {/* Form */}
<form onSubmit={formik.handleSubmit} className="space-y-4">
          {!isLogin && (
<>
<InputField label="Name" name="name" type="text" formik={formik} />
<label className="text-sm font-medium text-gray-700 mb-1">Role</label>
<select
                name="role"
                onChange={formik.handleChange}
                onBlur={formik.handleBlur}
                value={formik.values.role}
                className={`w-full px-4 py-2 border rounded-md focus:ring-2 focus:ring-indigo-500 focus:outline-none transition ${
                  formik.touched.role && formik.errors.role
                    ? 'border-red-400'
                    : 'border-gray-300'
                }`}
>
<option value="">Select a role</option>
<option value="USER">User</option>
<option value="ORGANIZER">Organizer</option>
<option value="SPEAKER">Speaker</option>
</select>
              {formik.touched.role && formik.errors.role && (
<p className="text-xs text-red-500 mt-1">{formik.errors.role}</p>
              )}
<InputField label="Contact" name="contact" type="text" formik={formik} />
</>
          )}
<InputField label="Email" name="email" type="email" formik={formik} />
<InputField label="Password" name="password" type="password" formik={formik} />
          {!isLogin && (
<InputField label="Confirm Password" name="confirmPassword" type="password" formik={formik} />
          )}
<button
            type="submit"
            className="w-full py-2 px-4 bg-indigo-600 hover:bg-indigo-700 text-white font-semibold rounded-lg shadow-sm transition"
            disabled={formik.isSubmitting}
>
            {isLogin ? 'Login' : 'Sign Up'}
</button>
</form>
        {/* Toggle Mode */}
<p className="mt-5 text-sm text-center text-gray-600">
          {isLogin ? "Don't have an account?" : 'Already have an account?'}{' '}
<button
            type="button"
            onClick={toggleMode}
            className="text-indigo-600 font-semibold hover:underline"
>
            {isLogin ? 'Sign Up' : 'Login'}
</button>
</p>
</div>
</div>
  );
};
const InputField = ({ label, name, type, formik }) => (
<div className="flex flex-col">
<label className="text-sm font-medium text-gray-700 mb-1">{label}</label>
<input
      type={type}
      name={name}
      onChange={formik.handleChange}
      onBlur={formik.handleBlur}
      value={formik.values[name]}
      className={`w-full px-4 py-2 border rounded-md focus:ring-2 focus:ring-indigo-500 focus:outline-none transition ${
        formik.touched[name] && formik.errors[name]
          ? 'border-red-400'
          : 'border-gray-300'
      }`}
    />
    {formik.touched[name] && formik.errors[name] && (
<p className="text-xs text-red-500 mt-1">{formik.errors[name]}</p>
    )}
</div>
);
export default AuthPopup;
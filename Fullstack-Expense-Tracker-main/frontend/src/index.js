import React from 'react';
import ReactDOM from 'react-dom/client';
import './index.css';
import App from './App';
import {BrowserRouter} from 'react-router-dom'
import axios from 'axios';
import AuthService from './services/auth.service';

// Add axios interceptor to handle 401 errors automatically
let isRedirecting = false; // Flag to prevent multiple redirects

axios.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response && error.response.status === 401) {
      const currentPath = window.location.pathname;
      const requestUrl = error.config && error.config.url ? error.config.url : '';
      
      // For export report downloads, don't clear session or redirect; let the page show an error instead
      if (requestUrl.includes('/report/exportTransactions')) {
        console.warn('401 on exportTransactions request - likely expired token. Not redirecting automatically.');
        return Promise.reject(error);
      }
      
      // Don't redirect if already on login/register/unauthorized pages or if already redirecting
      if (currentPath.includes('/auth/login') || 
          currentPath.includes('/auth/register') || 
          currentPath.includes('/unauthorized') ||
          isRedirecting) {
        return Promise.reject(error);
      }
      
      // Check if user exists but token is invalid
      const user = AuthService.getCurrentUser();
      if (user) {
        // For admin pages, don't redirect immediately - let the component show the error
        // This gives the user a chance to see what went wrong
        if (currentPath.includes('/admin/')) {
          console.warn('401 Unauthorized on admin page - Token may be expired or invalid. Please log out and log back in.');
          // Don't redirect, let the component handle the error
          return Promise.reject(error);
        }
        
        console.warn('401 Unauthorized - Token may be expired or invalid. Clearing session and redirecting to login.');
        isRedirecting = true; // Set flag to prevent multiple redirects
        AuthService.logout_req();
        
        // Small delay to ensure localStorage is cleared before redirect
        setTimeout(() => {
          isRedirecting = false; // Reset flag after redirect
          window.location.href = '/auth/login';
        }, 100);
      } else {
        // No user, just reject the error (let the component handle it)
        console.warn('401 Unauthorized - No user found in session');
      }
    }
    return Promise.reject(error);
  }
);

const root = ReactDOM.createRoot(document.getElementById('root'));
root.render(
  
    <BrowserRouter>

      <App />
    
    </BrowserRouter>
);


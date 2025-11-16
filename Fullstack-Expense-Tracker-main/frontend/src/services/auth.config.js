// Use environment variable for API URL, fallback to localhost for development
const API_BASE_URL = process.env.REACT_APP_API_BASE_URL || "http://localhost:8080/mypockit";

export default API_BASE_URL;
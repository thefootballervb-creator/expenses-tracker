import axios from "axios";
import AuthService from "./auth.service";
import API_BASE_URL from "./auth.config";

const getAllTransactions = (pagenumber, pageSize, searchKey) => {
    return axios.get(
        API_BASE_URL + "/transaction/getAll",
        {
            headers: AuthService.authHeader(),
            params: {
                pageNumber: pagenumber,
                pageSize: pageSize,
                searchKey: searchKey
            }
        }
    )
}

const getAllUsers = (pagenumber, pageSize, searchKey) => {
    const headers = AuthService.authHeader()
    const user = AuthService.getCurrentUser()
    
    console.log("getAllUsers - User:", user?.email)
    console.log("getAllUsers - User roles:", user?.roles)
    console.log("getAllUsers - Headers:", headers)
    console.log("getAllUsers - Token present:", !!headers.Authorization)
    
    // Decode JWT token to see what's inside
    if (user?.token) {
        try {
            const tokenParts = user.token.split('.')
            if (tokenParts.length === 3) {
                const payload = JSON.parse(atob(tokenParts[1]))
                console.log("getAllUsers - JWT payload:", payload)
                console.log("getAllUsers - JWT roles in token:", payload.roles || payload.authorities || 'No roles found in token')
            }
        } catch (e) {
            console.error("getAllUsers - Error decoding token:", e)
        }
    }
    
    return axios.get(
        API_BASE_URL + "/user/getAll",
        {
            headers: headers,
            params: {
                pageNumber: pagenumber,
                pageSize: pageSize,
                searchKey: searchKey
            }
        }
    )
}

const disableOrEnableUser = (userId) => {
    return axios.delete(
        API_BASE_URL + "/user/disable",
        {
            headers: AuthService.authHeader(),
            params: {
                userId: userId
            }
        }
    )
}

const getAllcategories = () => {
    return axios.get(
        API_BASE_URL + '/category/getAll', 
        {
            headers: AuthService.authHeader()
        }
    )
}

const addNewcategory = (categoryName, transactionTypeId) => {
    return axios.post(
        API_BASE_URL + '/category/new', 
        {
            categoryName: categoryName,
            transactionTypeId: transactionTypeId
        },
        {
            headers: AuthService.authHeader()
        }
    )
}

const updatecategory = (categoryId, categoryName, transactionTypeId) => {
    return axios.put(
        API_BASE_URL + '/category/update', 
        {
            categoryName: categoryName,
            transactionTypeId: transactionTypeId
        },
        {
            headers: AuthService.authHeader(),
            params: {
                categoryId: categoryId
            }
        }
    )
}

const disableOrEnableCategory = (categoryId) => {
    return axios.delete(
        API_BASE_URL + "/category/delete",
        {
            headers: AuthService.authHeader(),
            params: {
                categoryId: categoryId
            }
        }
    )
}

const AdminService = {
    getAllTransactions,
    getAllUsers,
    disableOrEnableUser,
    getAllcategories,
    addNewcategory,
    updatecategory,
    disableOrEnableCategory,
}

export default AdminService;
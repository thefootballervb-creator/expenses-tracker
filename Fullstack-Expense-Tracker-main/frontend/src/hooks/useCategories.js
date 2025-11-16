import { useEffect, useState } from "react";
import UserService from "../services/userService";
import toast from "react-hot-toast";

function useCategories() {
    const [categories, setCategories] = useState([]);
    const [isFetching,  setIsFetching] = useState(true);

    useEffect(() => {
        const getCategories = async () => {
            setIsFetching(true)
            try {
                const response = await UserService.get_categories()
                if (response && response.data && response.data.status === "SUCCESS") {
                    setCategories(response.data.response || [])
                } else {
                    toast.error("Failed to fetch all categories: Try again later!")
                }
            } catch (error) {
                console.error("Error fetching categories:", error)
                if (error.response && error.response.data && error.response.data.response) {
                    toast.error(error.response.data.response)
                } else {
                    toast.error("Failed to fetch all categories: Try again later!")
                }
            } finally {
                setIsFetching(false)
            }
        }

        getCategories()
    }, [])

    return [categories, isFetching];
}

export default useCategories;
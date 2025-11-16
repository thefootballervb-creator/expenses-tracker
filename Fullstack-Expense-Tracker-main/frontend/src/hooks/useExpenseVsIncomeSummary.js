import { useEffect, useState } from "react";
import UserService from "../services/userService";
import AuthService from "../services/auth.service";

function useExpenseVsIncomeSummary(months, refreshKey = 0) {
    const [data, setData] = useState([]);
    const [isError, setIsError] = useState(false);
    const [isLoading, setIsLoading] = useState(true)

    useEffect(() => {
        let isMounted = true
        
        const getData = async () => {
            setIsLoading(true)
            setIsError(false)
            try {
                const user = AuthService.getCurrentUser()
                if (!user || !user.email) {
                    console.error("User not found or not authenticated")
                    if (isMounted) {
                        setIsError(true)
                        setIsLoading(false)
                    }
                    return
                }

                const response = await UserService.getMonthlySummary(user.email)
                if (isMounted) {
                    if (response && response.data && response.data.status === "SUCCESS") {
                        const fetchedData = response.data.response || []
                        console.log("Fetched monthly summary data:", fetchedData)
                        generateData(fetchedData)
                    } else {
                        console.error("Failed to fetch monthly summary:", response)
                        setIsError(true)
                    }
                }
            } catch (error) {
                console.error("Error fetching monthly summary:", error)
                console.error("Error details:", {
                    message: error.message,
                    response: error.response?.data
                })
                if (isMounted) {
                    setIsError(true)
                }
            } finally {
                if (isMounted) {
                    setIsLoading(false)
                }
            }
        }

        getData()
        
        return () => {
            isMounted = false
        }
    }, [months, refreshKey])

    const generateData = (fetchedData) => {
        console.log("Generating data from:", fetchedData)
        console.log("Months to map:", months)
        const finalData = months.map(({ id, monthName }) => {
            const monthData = fetchedData.find((t) => {
                // Match by month number (1-12)
                const monthMatch = t.month === id
                console.log(`Checking month ${id} (${monthName}):`, { 
                    backendMonth: t.month, 
                    frontendMonth: id, 
                    matches: monthMatch,
                    data: t 
                })
                return monthMatch
            })
            const result = {
                id, monthName,
                totalIncome: monthData ? (monthData.total_income || 0) : 0,
                totalExpense: monthData ? (monthData.total_expense || 0) : 0
            }
            if (monthData) {
                console.log(`Matched data for ${monthName}:`, result)
            }
            return result
        })
        console.log("Final generated data:", finalData)
        setData(finalData)
    }

    return [data, isLoading, isError]
}

export default useExpenseVsIncomeSummary;
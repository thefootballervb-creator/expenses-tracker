import {useEffect, useState} from 'react';
import '../../../assets/styles/register.css';
import {useForm} from 'react-hook-form';
import { Link, useNavigate} from 'react-router-dom';
import AuthService from '../../../services/auth.service';
import Logo from '../../../components/utils/Logo';

function Login() {

    const navigate = useNavigate();

    useEffect(() => {
        const user = AuthService.getCurrentUser();
        // Only redirect if user exists AND has valid roles
        // Don't redirect if we came here due to a 401 error (session was cleared)
        if (user && user.roles && user.roles.length > 0) {
            // Check ROLE_ADMIN first since admins have both roles
            if (user.roles.includes("ROLE_ADMIN")) {
                navigate("/admin/transactions");
            } else if (user.roles.includes("ROLE_USER")) {
                navigate("/user/dashboard");
            }
        }
        // If no user or no roles, stay on login page
    }, [navigate])


    const {register, handleSubmit,formState} = useForm();

    const [response_error, setResponseError] = useState("");
    const [isLoading, setIsLoading] = useState(false);

    const onSubmit = async (data) => {
        setIsLoading(true)        
        try {
            await AuthService.login_req(data.email, data.password);
            setResponseError("");
            localStorage.setItem("message", JSON.stringify({ status: "SUCCESS", text: "Login successfull!" }))
            // Navigation will happen via window.location.reload() and useEffect
            // But we can also navigate directly after a short delay to ensure user data is loaded
            setTimeout(() => {
                const user = AuthService.getCurrentUser();
                if (user && user.roles) {
                    // Check ROLE_ADMIN first since admins have both roles
                    if (user.roles.includes("ROLE_ADMIN")) {
                        navigate("/admin/transactions");
                    } else if (user.roles.includes("ROLE_USER")) {
                        navigate("/user/dashboard");
                    }
                }
            }, 100) // Small delay to ensure localStorage is updated
        } catch (error) {
            const resMessage = (error.response && error.response.data && error.response.data.message) || error.message || error.toString();
            console.log(resMessage);
            if (resMessage === "Bad credentials"){
                setResponseError("Invalid email or password!");
            } else {
                setResponseError("Something went wrong: Try again later!");
            }
        } finally {
            setIsLoading(false);
        }
    }

    return(
        <div className='container'>
            <form className="auth-form"  onSubmit={handleSubmit(onSubmit)}>
            <Logo/>
                <h2>Login</h2>
                {
                    (response_error!=="") && <p>{response_error}</p>
                }
                
                <div className='input-box'>
                    <label>Email</label><br/>
                    <input 
                        type='text'
                        {...register('email', {
                            required: "Email is required!",
                            pattern: {value:/^[\w-.]+@([\w-]+\.)+[\w-]{2,4}$/g, message:"Invalid email address!"}
                        })}
                    />
                    {formState.errors.email && <small>{formState.errors.email.message}</small>}
                </div>
                
                <div className='input-box'>
                    <label>Password</label><br/>
                    <input 
                        type='password'
                        {
                            ...register('password', {
                                required: 'Password is required!'
                            })
                        }
                    />
                    {formState.errors.password && <small>{formState.errors.password.message}</small>}
                </div>
                <div className='msg'> <Link to={'/auth/forgetpassword/verifyEmail'} className='inline-link'>Forgot password?</Link></div><br/>
                
                <div className='input-box'>
                    <input type='submit' value={isLoading ? "Logging in..." : 'Login'}
                        className={isLoading ? "button button-fill loading" : "button button-fill"}
                    />
                </div>
                <br/><div className='msg'>New member? <Link to='/auth/register' className='inline-link'>Register Here</Link></div>
            </form>
        </div>
    )
}

export default Login;
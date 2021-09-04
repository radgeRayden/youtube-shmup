let glfw = (import .FFI.glfw)
let wgpu = (import .FFI.wgpu)

inline &local (T ...)
    &
        local T
            ...

global window : (mutable@ glfw.window)

fn init ()
    let status = (glfw.Init)
    if (not status)
        exit -1

    window = (glfw.CreateWindow 800 600 "my little game" null null)
    if (window == null)
        glfw.Terminate;
        exit -1
    ;

# This helper queries internal window handles used by the OS (as opposed to the GLFW window handle).
# These are used when initializing certain graphics APIs that own the window surface.
fn get-native-window-info ()
    """"Returns information necessary to initialize a window surface (webgpu, vulkan).
        On Linux, returns (:_ X11Display X11Window);
        On Windows, returns (:_ ModuleHandle HWND)
    static-match operating-system
    case 'linux
        let GetX11Display =
            extern 'glfwGetX11Display (function (mutable@ voidstar))
        let GetX11Window =
            extern 'glfwGetX11Window (function u64 (mutable@ glfw.window))
        _ (GetX11Display) (GetX11Window window)
    case 'windows
        let GetModuleHandleA =
            extern 'GetModuleHandleA (function voidstar voidstar)
        let GetWin32Window =
            extern 'glfwGetWin32Window (function voidstar (mutable@ glfw.window))
        _ (GetModuleHandleA null) (GetWin32Window window)
    default
        error "OS not supported"

fn create-wgpu-surface ()
    static-match operating-system
    case 'linux
        let x11-display x11-window = (get-native-window-info)
        wgpu.InstanceCreateSurface null
            &local wgpu.SurfaceDescriptor
                nextInChain =
                    as
                        &local wgpu.SurfaceDescriptorFromXlib
                            chain =
                                wgpu.ChainedStruct
                                    sType = wgpu.SType.SurfaceDescriptorFromXlib
                            display = (x11-display as voidstar)
                            window = (x11-window as u32)
                        mutable@ wgpu.ChainedStruct
    case 'windows
        let hinstance hwnd = (get-native-window-info)
        wgpu.InstanceCreateSurface null
            &local wgpu.SurfaceDescriptor
                nextInChain =
                    as
                        &local wgpu.SurfaceDescriptorFromWindowsHWND
                            chain =
                                wgpu.ChainedStruct
                                    sType = wgpu.SType.SurfaceDescriptorFromWindowsHWND
                            hinstance = hinstance
                            hwnd = hwnd
                        mutable@ wgpu.ChainedStruct
    default
        error "OS not supported"

fn get-size ()
    local w : i32
    local h : i32
    glfw.GetWindowSize window &w &h
    _ w h

do
    let window
    let init create-wgpu-surface get-size
    locals;
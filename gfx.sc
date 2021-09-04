using import struct
let wgpu = (import .FFI.wgpu)
import .window

struct GfxState plain
    adapter : wgpu.Adapter
    device : wgpu.Device
    swapchain : wgpu.SwapChain
    surface : wgpu.Surface
    
global istate : GfxState

fn recreate-swapchain ()
    let w h = (window.get-size)
    local swapchain-desc =
        wgpu.SwapChainDescriptor
            label = "my swapchain"
            usage = wgpu.TextureUsage.RenderAttachment
            format = wgpu.TextureFormat.BGRA8UnormSrgb
            width = (w as u32)
            height = (h as u32)
            presentMode = wgpu.PresentMode.Fifo
    istate.swapchain =
        wgpu.DeviceCreateSwapChain istate.device  istate.surface &swapchain-desc

fn init ()
    istate.surface = (window.create-wgpu-surface)

    # local instance-desc = (wgpu.InstanceDescriptor)
    # let instance =
    #     wgpu.CreateInstance &instance-desc

    local adapter-options =
        wgpu.RequestAdapterOptions
            compatibleSurface = istate.surface

    wgpu.InstanceRequestAdapter null &adapter-options
        fn (result userdata)
            istate.adapter = result
        null

    local device-desc = (wgpu.DeviceDescriptor)
    wgpu.AdapterRequestDevice istate.adapter &device-desc 
        fn (result userdata)
            istate.device = result
        null

    recreate-swapchain; 
    ;

fn present (clear-color)
    let cur-image = 
        wgpu.SwapChainGetCurrentTextureView istate.swapchain
    if (cur-image == null)
        recreate-swapchain;
        return;

    local cmd-enc-desc = 
        wgpu.CommandEncoderDescriptor
            label = "our command encoder"

    let cmd-encoder = (wgpu.DeviceCreateCommandEncoder istate.device &cmd-enc-desc)

    local color-attachment = 
        wgpu.RenderPassColorAttachment
            view = cur-image
            clearColor = (wgpu.Color (unpack clear-color))

    local rp-desc =
        wgpu.RenderPassDescriptor
            label = "main render pass"
            colorAttachmentCount = 1:u32
            colorAttachments = &color-attachment

    let rp = (wgpu.CommandEncoderBeginRenderPass cmd-encoder &rp-desc)
    wgpu.RenderPassEncoderEndPass rp

    local cmd-buf-desc =
        wgpu.CommandBufferDescriptor
            label = "our command buffer"
    local cmd-buf = (wgpu.CommandEncoderFinish cmd-encoder &cmd-buf-desc)
    let queue = (wgpu.DeviceGetQueue istate.device)
    wgpu.QueueSubmit queue 1 &cmd-buf

    wgpu.SwapChainPresent istate.swapchain

do 
    let init present
    locals;
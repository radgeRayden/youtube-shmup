using import glm
import .runtime
import .gfx
import .window

let glfw = (import .FFI.glfw)

window.init;
gfx.init;

while (not (glfw.WindowShouldClose window.window))
    glfw.PollEvents;
    gfx.present (vec4 0.017 0.017 0.017 1)

glfw.Terminate;
<template>
  <div ref="container" class="three-container"></div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount } from 'vue'
import * as THREE from 'three'
import { OrbitControls } from 'three/examples/jsm/controls/OrbitControls.js'
import { GLTFLoader } from 'three/examples/jsm/loaders/GLTFLoader.js'
import Stats from 'three/examples/jsm/libs/stats.module.js'
import { RoomEnvironment } from 'three/examples/jsm/environments/RoomEnvironment.js'

const container = ref(null)
let renderer, scene, camera, controls, mixer, stats, clock, pmremGenerator

const props = defineProps({
  modelPath: { type: String, required: true },
})

onMounted(() => {
  clock = new THREE.Clock()

  // Renderer
  renderer = new THREE.WebGLRenderer({ antialias: true })
  renderer.setPixelRatio(window.devicePixelRatio)
  renderer.setSize(window.innerWidth, window.innerHeight)
  container.value.appendChild(renderer.domElement)

  // Stats
  // stats = Stats()
  // container.value.appendChild(stats.dom)

  // Scene
  scene = new THREE.Scene()
  scene.background = new THREE.Color(0xbfe3dd)

  // PMREM + 环境贴图
  pmremGenerator = new THREE.PMREMGenerator(renderer)
  scene.environment = pmremGenerator.fromScene(new RoomEnvironment(), 0.04).texture

  // Camera
  camera = new THREE.PerspectiveCamera(40, window.innerWidth / window.innerHeight, 0.1, 100)
  camera.position.set(5, 2, 8)

  // Controls
  controls = new OrbitControls(camera, renderer.domElement)
  controls.target.set(0, 0.5, 0)
  controls.enablePan = false
  controls.enableDamping = true
  controls.update()

  // 添加光源
  const ambientLight = new THREE.AmbientLight(0xffffff, 0.6)
  scene.add(ambientLight)

  const directionalLight = new THREE.DirectionalLight(0xffffff, 1)
  directionalLight.position.set(5, 10, 7.5)
  scene.add(directionalLight)

  // Load GLB model
  const loader = new GLTFLoader()
  loader.load(
    props.modelPath,
    (gltf) => {
      const model = gltf.scene
      scene.add(model)

      // 自动缩放和居中
      const box = new THREE.Box3().setFromObject(model)
      const size = box.getSize(new THREE.Vector3())
      const maxAxis = Math.max(size.x, size.y, size.z)
      model.scale.multiplyScalar(1 / maxAxis)

      box.setFromObject(model)
      const center = box.getCenter(new THREE.Vector3())
      model.position.sub(center)

      // 动画
      if (gltf.animations && gltf.animations.length > 0) {
        mixer = new THREE.AnimationMixer(model)
        gltf.animations.forEach((clip) => {
          mixer.clipAction(clip).play()
        })
      }

      // 开始渲染循环
      renderer.setAnimationLoop(animate)
    },
    undefined,
    (error) => {
      console.error('GLB加载失败', error)
    },
  )

  window.addEventListener('resize', onWindowResize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', onWindowResize)
  renderer.dispose()
})

function onWindowResize() {
  camera.aspect = window.innerWidth / window.innerHeight
  camera.updateProjectionMatrix()
  renderer.setSize(window.innerWidth, window.innerHeight)
}

function animate() {
  const delta = clock.getDelta()
  if (mixer) mixer.update(delta)
  controls.update()
  // stats.update()
  renderer.render(scene, camera)
}
</script>

<style>
.three-container {
  width: 100vw;
  height: 100vh;
}
</style>

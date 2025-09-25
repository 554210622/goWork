<template>
  <div ref="container" class="three-container"></div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount, watch } from 'vue'
import * as THREE from 'three'
import { OrbitControls } from 'three/examples/jsm/controls/OrbitControls.js'
import { GLTFLoader } from 'three/examples/jsm/loaders/GLTFLoader.js'
import Stats from 'three/examples/jsm/libs/stats.module.js'
import { RoomEnvironment } from 'three/examples/jsm/environments/RoomEnvironment.js'

const container = ref(null)
let renderer, scene, camera, controls, mixer, stats, clock, pmremGenerator
let currentModel = null
let gridHelper = null
let ambientLight = null
let directionalLight = null

const props = defineProps({
  modelPath: { type: String, required: true },
  lightIntensity: { type: Number, default: 1.0 },
  showGrid: { type: Boolean, default: false },
  showStats: { type: Boolean, default: false },
})

const emit = defineEmits(['model-loaded'])

onMounted(() => {
  initThreeJS()
  loadModel(props.modelPath)
  window.addEventListener('resize', onWindowResize)
})

const initThreeJS = () => {
  clock = new THREE.Clock()

  // Renderer
  renderer = new THREE.WebGLRenderer({ antialias: true })
  renderer.setPixelRatio(window.devicePixelRatio)
  renderer.setSize(container.value.clientWidth, container.value.clientHeight)
  renderer.shadowMap.enabled = true
  renderer.shadowMap.type = THREE.PCFSoftShadowMap
  container.value.appendChild(renderer.domElement)

  // Stats
  stats = new Stats()
  if (props.showStats) {
    container.value.appendChild(stats.dom)
  }

  // Scene
  scene = new THREE.Scene()
  scene.background = new THREE.Color(0x2c3e50)

  // PMREM + 环境贴图
  pmremGenerator = new THREE.PMREMGenerator(renderer)
  scene.environment = pmremGenerator.fromScene(new RoomEnvironment(), 0.04).texture

  // Camera
  camera = new THREE.PerspectiveCamera(40, container.value.clientWidth / container.value.clientHeight, 0.1, 100)
  camera.position.set(5, 2, 8)

  // Controls
  controls = new OrbitControls(camera, renderer.domElement)
  controls.target.set(0, 0.5, 0)
  controls.enablePan = true
  controls.enableDamping = true
  controls.dampingFactor = 0.05
  controls.update()

  // 添加光源
  ambientLight = new THREE.AmbientLight(0xffffff, 0.6)
  scene.add(ambientLight)

  directionalLight = new THREE.DirectionalLight(0xffffff, props.lightIntensity)
  directionalLight.position.set(5, 10, 7.5)
  directionalLight.castShadow = true
  directionalLight.shadow.mapSize.width = 2048
  directionalLight.shadow.mapSize.height = 2048
  scene.add(directionalLight)

  // 网格辅助线
  gridHelper = new THREE.GridHelper(10, 10)
  gridHelper.visible = props.showGrid
  scene.add(gridHelper)

  // 开始渲染循环
  renderer.setAnimationLoop(animate)
}

const loadModel = (modelPath) => {
  // 移除旧模型
  if (currentModel) {
    scene.remove(currentModel)
    currentModel = null
  }

  // 停止旧动画
  if (mixer) {
    mixer.stopAllAction()
    mixer = null
  }

  // 加载新模型
  const loader = new GLTFLoader()
  loader.load(
    modelPath,
    (gltf) => {
      currentModel = gltf.scene
      scene.add(currentModel)

      // 自动缩放和居中
      const box = new THREE.Box3().setFromObject(currentModel)
      const size = box.getSize(new THREE.Vector3())
      const maxAxis = Math.max(size.x, size.y, size.z)
      currentModel.scale.multiplyScalar(2 / maxAxis)

      box.setFromObject(currentModel)
      const center = box.getCenter(new THREE.Vector3())
      currentModel.position.sub(center)

      // 启用阴影
      currentModel.traverse((child) => {
        if (child.isMesh) {
          child.castShadow = true
          child.receiveShadow = true
        }
      })

      // 动画
      if (gltf.animations && gltf.animations.length > 0) {
        mixer = new THREE.AnimationMixer(currentModel)
        gltf.animations.forEach((clip) => {
          mixer.clipAction(clip).play()
        })
      }

      // 发送模型加载完成事件
      emit('model-loaded', {
        name: modelPath.split('/').pop().replace('.glb', ''),
        triangles: getTriangleCount(currentModel),
        vertices: getVertexCount(currentModel)
      })
    },
    undefined,
    (error) => {
      console.error('GLB加载失败', error)
    }
  )
}

onBeforeUnmount(() => {
  window.removeEventListener('resize', onWindowResize)
  if (renderer) {
    renderer.dispose()
  }
  if (pmremGenerator) {
    pmremGenerator.dispose()
  }
})

// 监听属性变化
watch(() => props.modelPath, (newPath) => {
  if (newPath) {
    loadModel(newPath)
  }
})

watch(() => props.lightIntensity, (newIntensity) => {
  if (directionalLight) {
    directionalLight.intensity = newIntensity
  }
})

watch(() => props.showGrid, (show) => {
  if (gridHelper) {
    gridHelper.visible = show
  }
})

watch(() => props.showStats, (show) => {
  if (stats) {
    if (show && !container.value.contains(stats.dom)) {
      container.value.appendChild(stats.dom)
    } else if (!show && container.value.contains(stats.dom)) {
      container.value.removeChild(stats.dom)
    }
  }
})

function onWindowResize() {
  if (!camera || !renderer || !container.value) return
  
  const width = container.value.clientWidth
  const height = container.value.clientHeight
  
  camera.aspect = width / height
  camera.updateProjectionMatrix()
  renderer.setSize(width, height)
}

function animate() {
  const delta = clock.getDelta()
  if (mixer) mixer.update(delta)
  if (controls) controls.update()
  if (stats) stats.update()
  if (renderer && scene && camera) {
    renderer.render(scene, camera)
  }
}

// 工具函数
const getTriangleCount = (object) => {
  let triangles = 0
  object.traverse((child) => {
    if (child.isMesh && child.geometry) {
      if (child.geometry.index) {
        triangles += child.geometry.index.count / 3
      } else {
        triangles += child.geometry.attributes.position.count / 3
      }
    }
  })
  return Math.floor(triangles)
}

const getVertexCount = (object) => {
  let vertices = 0
  object.traverse((child) => {
    if (child.isMesh && child.geometry) {
      vertices += child.geometry.attributes.position.count
    }
  })
  return vertices
}

// 暴露给父组件的方法
const zoomIn = () => {
  if (camera && controls) {
    const direction = new THREE.Vector3()
    camera.getWorldDirection(direction)
    camera.position.add(direction.multiplyScalar(0.5))
    controls.update()
  }
}

const zoomOut = () => {
  if (camera && controls) {
    const direction = new THREE.Vector3()
    camera.getWorldDirection(direction)
    camera.position.add(direction.multiplyScalar(-0.5))
    controls.update()
  }
}

const resetCamera = () => {
  if (camera && controls) {
    camera.position.set(5, 2, 8)
    controls.target.set(0, 0.5, 0)
    controls.update()
  }
}

const setLightIntensity = (intensity) => {
  if (directionalLight) {
    directionalLight.intensity = intensity
  }
}

const toggleGrid = (show) => {
  if (gridHelper) {
    gridHelper.visible = show
  }
}

const toggleStats = (show) => {
  if (stats && container.value) {
    if (show && !container.value.contains(stats.dom)) {
      container.value.appendChild(stats.dom)
    } else if (!show && container.value.contains(stats.dom)) {
      container.value.removeChild(stats.dom)
    }
  }
}

const toggleModelVisibility = (visible) => {
  if (currentModel) {
    currentModel.visible = visible
  }
}

defineExpose({
  zoomIn,
  zoomOut,
  resetCamera,
  setLightIntensity,
  toggleGrid,
  toggleStats,
  toggleModelVisibility,
  loadModel
})
</script>

<style scoped>
.three-container {
  width: 100%;
  height: 100%;
  position: relative;
}
</style>

import AppKit
import Foundation

private let repoRoot = URL(fileURLWithPath: "/Users/guillermo.castella/bizi", isDirectory: true)
private let backgroundColor = NSColor(calibratedRed: 0.972, green: 0.965, blue: 0.965, alpha: 1.0)
private let redColor = NSColor(calibratedRed: 0.843, green: 0.098, blue: 0.122, alpha: 1.0)
private let whiteColor = NSColor.white

private struct AppleIconSpec {
  let filename: String
  let size: String
  let scale: String
  let idiom: String
  let role: String?
  let subtype: String?

  var pixelSize: Int {
    let base = size.split(separator: "x").first.flatMap { Double($0) } ?? 0
    let multiplier = Double(scale.dropLast()) ?? 1
    return Int((base * multiplier).rounded())
  }

  var json: [String: String] {
    var result: [String: String] = [
      "filename": filename,
      "size": size,
      "scale": scale,
      "idiom": idiom,
    ]
    if let role {
      result["role"] = role
    }
    if let subtype {
      result["subtype"] = subtype
    }
    return result
  }
}

private let iosIconSpecs: [AppleIconSpec] = [
  .init(filename: "iphone-notification-20@2x.png", size: "20x20", scale: "2x", idiom: "iphone", role: nil, subtype: nil),
  .init(filename: "iphone-notification-20@3x.png", size: "20x20", scale: "3x", idiom: "iphone", role: nil, subtype: nil),
  .init(filename: "iphone-settings-29@2x.png", size: "29x29", scale: "2x", idiom: "iphone", role: nil, subtype: nil),
  .init(filename: "iphone-settings-29@3x.png", size: "29x29", scale: "3x", idiom: "iphone", role: nil, subtype: nil),
  .init(filename: "iphone-spotlight-40@2x.png", size: "40x40", scale: "2x", idiom: "iphone", role: nil, subtype: nil),
  .init(filename: "iphone-spotlight-40@3x.png", size: "40x40", scale: "3x", idiom: "iphone", role: nil, subtype: nil),
  .init(filename: "iphone-app-60@2x.png", size: "60x60", scale: "2x", idiom: "iphone", role: nil, subtype: nil),
  .init(filename: "iphone-app-60@3x.png", size: "60x60", scale: "3x", idiom: "iphone", role: nil, subtype: nil),
  .init(filename: "ipad-notification-20@1x.png", size: "20x20", scale: "1x", idiom: "ipad", role: nil, subtype: nil),
  .init(filename: "ipad-notification-20@2x.png", size: "20x20", scale: "2x", idiom: "ipad", role: nil, subtype: nil),
  .init(filename: "ipad-settings-29@1x.png", size: "29x29", scale: "1x", idiom: "ipad", role: nil, subtype: nil),
  .init(filename: "ipad-settings-29@2x.png", size: "29x29", scale: "2x", idiom: "ipad", role: nil, subtype: nil),
  .init(filename: "ipad-spotlight-40@1x.png", size: "40x40", scale: "1x", idiom: "ipad", role: nil, subtype: nil),
  .init(filename: "ipad-spotlight-40@2x.png", size: "40x40", scale: "2x", idiom: "ipad", role: nil, subtype: nil),
  .init(filename: "ipad-app-76@1x.png", size: "76x76", scale: "1x", idiom: "ipad", role: nil, subtype: nil),
  .init(filename: "ipad-app-76@2x.png", size: "76x76", scale: "2x", idiom: "ipad", role: nil, subtype: nil),
  .init(filename: "ipad-pro-app-83.5@2x.png", size: "83.5x83.5", scale: "2x", idiom: "ipad", role: nil, subtype: nil),
  .init(filename: "ios-marketing-1024.png", size: "1024x1024", scale: "1x", idiom: "ios-marketing", role: nil, subtype: nil),
]

private let watchIconSpecs: [AppleIconSpec] = [
  .init(filename: "watch-notification-24@2x.png", size: "24x24", scale: "2x", idiom: "watch", role: "notificationCenter", subtype: "38mm"),
  .init(filename: "watch-notification-27.5@2x.png", size: "27.5x27.5", scale: "2x", idiom: "watch", role: "notificationCenter", subtype: "42mm"),
  .init(filename: "watch-settings-29@2x.png", size: "29x29", scale: "2x", idiom: "watch", role: "companionSettings", subtype: nil),
  .init(filename: "watch-settings-29@3x.png", size: "29x29", scale: "3x", idiom: "watch", role: "companionSettings", subtype: nil),
  .init(filename: "watch-launcher-40@2x.png", size: "40x40", scale: "2x", idiom: "watch", role: "appLauncher", subtype: "38mm"),
  .init(filename: "watch-launcher-44@2x.png", size: "44x44", scale: "2x", idiom: "watch", role: "appLauncher", subtype: "40mm"),
  .init(filename: "watch-launcher-46@2x.png", size: "46x46", scale: "2x", idiom: "watch", role: "appLauncher", subtype: "41mm"),
  .init(filename: "watch-launcher-50@2x.png", size: "50x50", scale: "2x", idiom: "watch", role: "appLauncher", subtype: "44mm"),
  .init(filename: "watch-launcher-51@2x.png", size: "51x51", scale: "2x", idiom: "watch", role: "appLauncher", subtype: "45mm"),
  .init(filename: "watch-launcher-54@2x.png", size: "54x54", scale: "2x", idiom: "watch", role: "appLauncher", subtype: "49mm"),
  .init(filename: "watch-quicklook-86@2x.png", size: "86x86", scale: "2x", idiom: "watch", role: "quickLook", subtype: "38mm"),
  .init(filename: "watch-quicklook-98@2x.png", size: "98x98", scale: "2x", idiom: "watch", role: "quickLook", subtype: "42mm"),
  .init(filename: "watch-quicklook-108@2x.png", size: "108x108", scale: "2x", idiom: "watch", role: "quickLook", subtype: "44mm"),
  .init(filename: "watch-quicklook-117@2x.png", size: "117x117", scale: "2x", idiom: "watch", role: "quickLook", subtype: "45mm"),
  .init(filename: "watch-quicklook-129@2x.png", size: "129x129", scale: "2x", idiom: "watch", role: "quickLook", subtype: "49mm"),
  .init(filename: "watch-marketing-1024.png", size: "1024x1024", scale: "1x", idiom: "watch-marketing", role: nil, subtype: nil),
]

private func point(_ x: CGFloat, _ y: CGFloat, in size: CGFloat) -> CGPoint {
  CGPoint(x: x * size, y: y * size)
}

private func writeIconPng(size: Int, to url: URL) throws {
  let canvasSize = CGFloat(size)
  guard let bitmap = NSBitmapImageRep(
    bitmapDataPlanes: nil,
    pixelsWide: size,
    pixelsHigh: size,
    bitsPerSample: 8,
    samplesPerPixel: 4,
    hasAlpha: true,
    isPlanar: false,
    colorSpaceName: .deviceRGB,
    bytesPerRow: 0,
    bitsPerPixel: 0
  ) else {
    throw NSError(domain: "IconGenerator", code: 1, userInfo: [NSLocalizedDescriptionKey: "Could not allocate bitmap."])
  }

  guard let context = NSGraphicsContext(bitmapImageRep: bitmap) else {
    throw NSError(domain: "IconGenerator", code: 2, userInfo: [NSLocalizedDescriptionKey: "Could not create graphics context."])
  }

  NSGraphicsContext.saveGraphicsState()
  NSGraphicsContext.current = context

  backgroundColor.setFill()
  NSBezierPath(rect: NSRect(x: 0, y: 0, width: canvasSize, height: canvasSize)).fill()

  let circleDiameter = canvasSize * 0.76
  let circleOrigin = (canvasSize - circleDiameter) / 2
  redColor.setFill()
  NSBezierPath(
    ovalIn: NSRect(x: circleOrigin, y: circleOrigin, width: circleDiameter, height: circleDiameter)
  ).fill()

  let wheelRadius = canvasSize * 0.105
  let lineWidth = canvasSize * 0.040
  let leftWheel = point(0.33, 0.36, in: canvasSize)
  let rightWheel = point(0.67, 0.36, in: canvasSize)
  let seat = point(0.45, 0.56, in: canvasSize)
  let frontJoint = point(0.58, 0.45, in: canvasSize)
  let handleBase = point(0.60, 0.59, in: canvasSize)
  let handleTip = point(0.665, 0.63, in: canvasSize)
  let pedal = point(0.50, 0.44, in: canvasSize)
  let hip = point(0.54, 0.65, in: canvasSize)
  let shoulder = point(0.595, 0.605, in: canvasSize)
  let headCenter = point(0.58, 0.75, in: canvasSize)
  let headRadius = canvasSize * 0.05

  whiteColor.setStroke()
  whiteColor.setFill()

  func strokedPath(_ builder: (NSBezierPath) -> Void) {
    let path = NSBezierPath()
    path.lineWidth = lineWidth
    path.lineCapStyle = .round
    path.lineJoinStyle = .round
    builder(path)
    path.stroke()
  }

  let leftWheelPath = NSBezierPath(ovalIn: NSRect(
    x: leftWheel.x - wheelRadius,
    y: leftWheel.y - wheelRadius,
    width: wheelRadius * 2,
    height: wheelRadius * 2
  ))
  leftWheelPath.lineWidth = lineWidth
  leftWheelPath.stroke()

  let rightWheelPath = NSBezierPath(ovalIn: NSRect(
    x: rightWheel.x - wheelRadius,
    y: rightWheel.y - wheelRadius,
    width: wheelRadius * 2,
    height: wheelRadius * 2
  ))
  rightWheelPath.lineWidth = lineWidth
  rightWheelPath.stroke()

  strokedPath { path in
    path.move(to: leftWheel)
    path.line(to: seat)
    path.line(to: frontJoint)
    path.line(to: rightWheel)
  }

  strokedPath { path in
    path.move(to: leftWheel)
    path.line(to: pedal)
    path.line(to: frontJoint)
  }

  strokedPath { path in
    path.move(to: frontJoint)
    path.line(to: handleBase)
    path.line(to: handleTip)
  }

  strokedPath { path in
    path.move(to: point(0.41, 0.585, in: canvasSize))
    path.line(to: point(0.47, 0.585, in: canvasSize))
  }

  strokedPath { path in
    path.move(to: hip)
    path.line(to: point(0.50, 0.58, in: canvasSize))
    path.line(to: seat)
  }

  strokedPath { path in
    path.move(to: shoulder)
    path.line(to: handleBase)
  }

  strokedPath { path in
    path.move(to: hip)
    path.line(to: pedal)
  }

  let headPath = NSBezierPath(ovalIn: NSRect(
    x: headCenter.x - headRadius,
    y: headCenter.y - headRadius,
    width: headRadius * 2,
    height: headRadius * 2
  ))
  headPath.fill()

  context.flushGraphics()
  NSGraphicsContext.restoreGraphicsState()

  guard let pngData = bitmap.representation(using: .png, properties: [:]) else {
    throw NSError(domain: "IconGenerator", code: 3, userInfo: [NSLocalizedDescriptionKey: "Could not encode PNG."])
  }
  try FileManager.default.createDirectory(at: url.deletingLastPathComponent(), withIntermediateDirectories: true)
  try pngData.write(to: url)
}

private func writeContentsJson(images: [[String: String]], to url: URL) throws {
  let payload: [String: Any] = [
    "images": images,
    "info": [
      "version": 1,
      "author": "xcode",
    ],
  ]
  let data = try JSONSerialization.data(withJSONObject: payload, options: [.prettyPrinted, .sortedKeys])
  try FileManager.default.createDirectory(at: url.deletingLastPathComponent(), withIntermediateDirectories: true)
  try data.write(to: url)
}

private func writeCatalogRoot(at url: URL) throws {
  let payload: [String: Any] = [
    "info": [
      "version": 1,
      "author": "xcode",
    ],
  ]
  let data = try JSONSerialization.data(withJSONObject: payload, options: [.prettyPrinted, .sortedKeys])
  try FileManager.default.createDirectory(at: url, withIntermediateDirectories: true)
  try data.write(to: url.appendingPathComponent("Contents.json"))
}

private func generateAndroidIcons(module: String) throws {
  let sizes: [(directory: String, size: Int)] = [
    ("mipmap-mdpi", 48),
    ("mipmap-hdpi", 72),
    ("mipmap-xhdpi", 96),
    ("mipmap-xxhdpi", 144),
    ("mipmap-xxxhdpi", 192),
  ]
  for item in sizes {
    let base = repoRoot
      .appendingPathComponent(module)
      .appendingPathComponent("src/androidMain/res")
      .appendingPathComponent(item.directory)
    try writeIconPng(size: item.size, to: base.appendingPathComponent("ic_launcher.png"))
    try writeIconPng(size: item.size, to: base.appendingPathComponent("ic_launcher_round.png"))
  }
}

private func generateAppleCatalog(
  root: URL,
  specs: [AppleIconSpec]
) throws {
  try writeCatalogRoot(at: root)
  let iconSet = root.appendingPathComponent("AppIcon.appiconset")
  try writeCatalogRoot(at: iconSet)
  for spec in specs {
    try writeIconPng(size: spec.pixelSize, to: iconSet.appendingPathComponent(spec.filename))
  }
  try writeContentsJson(images: specs.map(\.json), to: iconSet.appendingPathComponent("Contents.json"))
}

do {
  try generateAndroidIcons(module: "androidApp")
  try generateAndroidIcons(module: "wearApp")
  try generateAppleCatalog(
    root: repoRoot.appendingPathComponent("apple/iosApp/Assets.xcassets"),
    specs: iosIconSpecs
  )
  try generateAppleCatalog(
    root: repoRoot.appendingPathComponent("apple/watchApp/Assets.xcassets"),
    specs: watchIconSpecs
  )
  print("Brand icons generated successfully.")
} catch {
  fputs("Failed to generate brand icons: \(error)\n", stderr)
  exit(1)
}

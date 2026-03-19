import AppKit
import Foundation

private let repoRoot = URL(fileURLWithPath: "/Users/guillermo.castella/bizi", isDirectory: true)
private let defaultSourceImagePath = repoRoot
  .appendingPathComponent("design")
  .appendingPathComponent("biciradar-app-icon-source.png")
  .path

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

private func loadSourceImage() throws -> NSImage {
  let sourcePath = CommandLine.arguments.dropFirst().first ?? defaultSourceImagePath
  let sourceUrl = URL(fileURLWithPath: sourcePath)
  guard let image = NSImage(contentsOf: sourceUrl) else {
    throw NSError(
      domain: "IconGenerator",
      code: 10,
      userInfo: [NSLocalizedDescriptionKey: "Could not load source image at \(sourceUrl.path)."]
    )
  }
  return image
}

private func drawSourceImage(_ image: NSImage, in canvasSize: CGFloat) {
  let sourceSize = image.size
  guard sourceSize.width > 0, sourceSize.height > 0 else { return }

  let scale = max(canvasSize / sourceSize.width, canvasSize / sourceSize.height)
  let drawWidth = sourceSize.width * scale
  let drawHeight = sourceSize.height * scale
  let drawRect = NSRect(
    x: (canvasSize - drawWidth) / 2,
    y: (canvasSize - drawHeight) / 2,
    width: drawWidth,
    height: drawHeight
  )

  image.draw(in: drawRect, from: .zero, operation: .sourceOver, fraction: 1.0)
}

private func writeIconPng(sourceImage: NSImage, size: Int, to url: URL) throws {
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

  NSColor.clear.setFill()
  NSBezierPath(rect: NSRect(x: 0, y: 0, width: canvasSize, height: canvasSize)).fill()
  drawSourceImage(sourceImage, in: canvasSize)

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
  let sourceImage = try loadSourceImage()
  for item in sizes {
    let base = repoRoot
      .appendingPathComponent(module)
      .appendingPathComponent("src/androidMain/res")
      .appendingPathComponent(item.directory)
    try writeIconPng(sourceImage: sourceImage, size: item.size, to: base.appendingPathComponent("ic_launcher.png"))
    try writeIconPng(sourceImage: sourceImage, size: item.size, to: base.appendingPathComponent("ic_launcher_round.png"))
  }
}

private func generateAppleCatalog(
  root: URL,
  specs: [AppleIconSpec]
) throws {
  let sourceImage = try loadSourceImage()
  try writeCatalogRoot(at: root)
  let iconSet = root.appendingPathComponent("AppIcon.appiconset")
  try writeCatalogRoot(at: iconSet)
  for spec in specs {
    try writeIconPng(sourceImage: sourceImage, size: spec.pixelSize, to: iconSet.appendingPathComponent(spec.filename))
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

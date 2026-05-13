package com.binge.GameProject.utils;

import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.TriangleMesh;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class OBJImporter {

    /**
     * @param resourcePath 資源路徑，例如 "/models/cube.obj"
     */
    public static MeshView loadModel(String resourcePath) {
        // 使用 getResourceAsStream 從 resources 資料夾讀取檔案
        try (InputStream is = OBJImporter.class.getResourceAsStream(resourcePath)) {
            if (is == null) {
                System.err.println("找不到資源檔案: " + resourcePath);
                return null;
            }

            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            TriangleMesh mesh = new TriangleMesh();

            List<Float> vertices = new ArrayList<>();
            List<Float> texCoords = new ArrayList<>();
            List<Integer> faces = new ArrayList<>();

            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                // 過濾掉註解或不支援的標記
                if (line.isEmpty() || line.startsWith("#") || line.startsWith("mtllib") ||
                        line.startsWith("usemtl") || line.startsWith("o ") ||
                        line.startsWith("s ") || line.startsWith("g ") || line.startsWith("vn ")) {
                    continue;
                }

                if (line.startsWith("v ")) {
                    String[] parts = line.split("\\s+");
                    vertices.add(Float.parseFloat(parts[1]));
                    vertices.add(Float.parseFloat(parts[2]));
                    vertices.add(Float.parseFloat(parts[3]));
                } else if (line.startsWith("vt ")) {
                    String[] parts = line.split("\\s+");
                    texCoords.add(Float.parseFloat(parts[1]));
                    // JavaFX 的 V 軸與 OBJ 相反，需要 1.0 - v
                    texCoords.add(1.0f - Float.parseFloat(parts[2]));
                } else if (line.startsWith("f ")) {
                    String[] parts = line.split("\\s+");
                    int len = parts.length - 1;
                    int[] vIndices = new int[len];
                    int[] tIndices = new int[len];

                    for (int i = 0; i < len; i++) {
                        String[] subParts = parts[i + 1].split("/");
                        vIndices[i] = Integer.parseInt(subParts[0]) - 1;
                        if (subParts.length > 1 && !subParts[1].isEmpty()) {
                            tIndices[i] = Integer.parseInt(subParts[1]) - 1;
                        } else {
                            tIndices[i] = 0;
                        }
                    }

                    // 三角化處理 (Fan Triangulation)
                    for (int i = 1; i < len - 1; i++) {
                        faces.add(vIndices[0]); faces.add(tIndices[0]);
                        faces.add(vIndices[i]); faces.add(tIndices[i]);
                        faces.add(vIndices[i+1]); faces.add(tIndices[i+1]);
                    }
                }
            }

            // 確保貼圖座標不為空
            if (texCoords.isEmpty()) {
                mesh.getTexCoords().addAll(0f, 0f);
            } else {
                float[] tArr = new float[texCoords.size()];
                for (int i = 0; i < texCoords.size(); i++) tArr[i] = texCoords.get(i);
                mesh.getTexCoords().addAll(tArr);
            }

            float[] vArr = new float[vertices.size()];
            for (int i = 0; i < vertices.size(); i++) vArr[i] = vertices.get(i);
            mesh.getPoints().addAll(vArr);

            int[] fArr = new int[faces.size()];
            for (int i = 0; i < faces.size(); i++) fArr[i] = faces.get(i);
            mesh.getFaces().addAll(fArr);

            MeshView meshView = new MeshView(mesh);
            PhongMaterial material = new PhongMaterial(Color.LIGHTGRAY);
            material.setSpecularColor(Color.WHITE);
            meshView.setMaterial(material);

            return meshView;
        } catch (Exception e) {
            System.err.println("載入 OBJ 失敗: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}
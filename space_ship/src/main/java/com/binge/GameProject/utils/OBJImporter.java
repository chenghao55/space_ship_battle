package com.binge.GameProject.utils;

import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.TriangleMesh;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

public class OBJImporter {

    public static MeshView loadModel(String objFilePath) {
        try (BufferedReader br = new BufferedReader(new FileReader(objFilePath))) {
            TriangleMesh mesh = new TriangleMesh();
            
            List<Float> vertices = new ArrayList<>();
            List<Float> texCoords = new ArrayList<>();
            List<Integer> faces = new ArrayList<>();
            
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#") || line.startsWith("mtllib") || line.startsWith("usemtl") || line.startsWith("o ") || line.startsWith("s ") || line.startsWith("g ")) {
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
                    texCoords.add(1.0f - Float.parseFloat(parts[2])); // JavaFX requires flipped V
                } else if (line.startsWith("f ")) {
                    String[] parts = line.split("\\s+");
                    int[] vIndices = new int[parts.length - 1];
                    int[] tIndices = new int[parts.length - 1];
                    
                    for (int i = 1; i < parts.length; i++) {
                        String[] subParts = parts[i].split("/");
                        vIndices[i-1] = Integer.parseInt(subParts[0]) - 1; // OBJ indices are 1-based
                        if (subParts.length > 1 && !subParts[1].isEmpty()) {
                            tIndices[i-1] = Integer.parseInt(subParts[1]) - 1;
                        } else {
                            tIndices[i-1] = 0;
                        }
                    }
                    
                    // Fan Triangulation for convex polygons (like quads)
                    for (int i = 1; i < vIndices.length - 1; i++) {
                        faces.add(vIndices[0]); faces.add(tIndices[0]);
                        faces.add(vIndices[i]); faces.add(tIndices[i]);
                        faces.add(vIndices[i+1]); faces.add(tIndices[i+1]);
                    }
                }
            }
            
            if (texCoords.isEmpty()) {
                texCoords.add(0f);
                texCoords.add(0f);
            }
            
            float[] vArr = new float[vertices.size()];
            for (int i = 0; i < vertices.size(); i++) vArr[i] = vertices.get(i);
            mesh.getPoints().addAll(vArr);
            
            float[] tArr = new float[texCoords.size()];
            for (int i = 0; i < texCoords.size(); i++) tArr[i] = texCoords.get(i);
            mesh.getTexCoords().addAll(tArr);
            
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

package com.numbercruncher.rainbow;

import com.numbercruncher.rainbow.ray_tools.HitRecord;
import com.numbercruncher.rainbow.ray_tools.Ray;

import java.util.Arrays;
import java.util.List;

/**
 * Bounding Volume Hierarchy node.
 * Accelerates ray-scene intersection from O(N) to O(log N).
 *
 * Each node is either:
 *  - A leaf holding a single SceneObject and its index
 *  - An internal node with two children and a bounding box enclosing both
 */
public class BVHNode {
    private final AABB box;
    private final BVHNode left, right;
    private final SceneObject leaf;
    private final int objectIndex;

    private BVHNode(AABB box, SceneObject leaf, int objectIndex) {
        this.box = box;
        this.leaf = leaf;
        this.objectIndex = objectIndex;
        this.left = null;
        this.right = null;
    }

    private BVHNode(AABB box, BVHNode left, BVHNode right) {
        this.box = box;
        this.left = left;
        this.right = right;
        this.leaf = null;
        this.objectIndex = -1;
    }

    /**
     * Build a BVH from the objects at the given indices.
     *
     * @param objects full object list (for getBounds() and later intersection)
     * @param indices indices into objects list for the bounded objects
     * @param start   inclusive start in indices array
     * @param end     exclusive end in indices array
     */
    public static BVHNode build(List<SceneObject> objects, int[] indices, int start, int end) {
        int count = end - start;

        if (count == 1) {
            int idx = indices[start];
            return new BVHNode(objects.get(idx).getBounds(), objects.get(idx), idx);
        }

        if (count == 2) {
            int i0 = indices[start], i1 = indices[start + 1];
            BVHNode l = new BVHNode(objects.get(i0).getBounds(), objects.get(i0), i0);
            BVHNode r = new BVHNode(objects.get(i1).getBounds(), objects.get(i1), i1);
            return new BVHNode(AABB.surrounding(l.box, r.box), l, r);
        }

        // Compute total bounds
        AABB totalBox = objects.get(indices[start]).getBounds();
        for (int i = start + 1; i < end; i++) {
            totalBox = AABB.surrounding(totalBox, objects.get(indices[i]).getBounds());
        }

        // Pick the longest axis to split along
        double xLen = totalBox.max.x - totalBox.min.x;
        double yLen = totalBox.max.y - totalBox.min.y;
        double zLen = totalBox.max.z - totalBox.min.z;
        int axis = (xLen >= yLen && xLen >= zLen) ? 0 : (yLen >= zLen) ? 1 : 2;

        // Sort the sub-range by centroid along the chosen axis
        sortByAxis(objects, indices, start, end, axis);

        int mid = (start + end) / 2;
        BVHNode l = build(objects, indices, start, mid);
        BVHNode r = build(objects, indices, mid, end);

        return new BVHNode(AABB.surrounding(l.box, r.box), l, r);
    }

    private static void sortByAxis(List<SceneObject> objects, int[] indices, int start, int end, int axis) {
        Integer[] sortable = new Integer[end - start];
        for (int i = 0; i < sortable.length; i++) {
            sortable[i] = indices[start + i];
        }
        Arrays.sort(sortable, (a, b) -> {
            double ca = centroid(objects.get(a).getBounds(), axis);
            double cb = centroid(objects.get(b).getBounds(), axis);
            return Double.compare(ca, cb);
        });
        for (int i = 0; i < sortable.length; i++) {
            indices[start + i] = sortable[i];
        }
    }

    private static double centroid(AABB box, int axis) {
        return 0.5 * (box.min.get(axis) + box.max.get(axis));
    }

    /**
     * Find the closest intersection in this subtree.
     * The tInterval is narrowed as closer hits are found, so sibling
     * subtrees can be skipped via the AABB test.
     */
    public HitRecord intersect(Ray ray, Interval tInterval) {
        if (!box.hit(ray, tInterval.min, tInterval.max)) return null;

        if (leaf != null) {
            HitRecord record = leaf.intersect(ray);
            if (record != null && tInterval.contains(record.t)) {
                record.objectIndex = objectIndex;
                tInterval.shrinkTo(record.t);
                return record;
            }
            return null;
        }

        HitRecord leftHit = left.intersect(ray, tInterval);
        HitRecord rightHit = right.intersect(ray, tInterval);

        return (rightHit != null) ? rightHit : leftHit;
    }
}
